#include <jni.h>
#include <openxr/openxr.h>
#include <openxr/openxr_platform.h>
#include <string>
#include <vector>
#include <cstring>
#include <chrono>
#include <thread>
#include <limits>
#include <cmath>
#include <time.h>

// Public loader/runtime query only. Never accesses the Spatial Engine's private instance/session.
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_blinkisland_capabilityprobe_platform_OpenXrSupport_queryNative(JNIEnv* env,jobject,jobject activity,jboolean faceAllowed){
 JavaVM* vm=nullptr;
 if(env->GetJavaVM(&vm)!=JNI_OK)return env->NewStringUTF("{\"stage\":\"jvm\",\"ok\":false}");
 std::string json="{";
 auto code=[&](const char* stage,XrResult r){json+="\""+std::string(stage)+"\":"+std::to_string(r)+",";};
 auto finish=[&](const char* reason){json+="\"conclusion\":\""+std::string(reason)+"\"}";return env->NewStringUTF(json.c_str());};
 PFN_xrInitializeLoaderKHR init=nullptr;
 auto r=xrGetInstanceProcAddr(XR_NULL_HANDLE,"xrInitializeLoaderKHR",reinterpret_cast<PFN_xrVoidFunction*>(&init));code("get_init",r);
 if(XR_FAILED(r)||!init)return finish("loader_init_function_unavailable");
 XrLoaderInitInfoAndroidKHR loader{XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR};loader.applicationVM=vm;loader.applicationContext=activity;
 r=init(reinterpret_cast<const XrLoaderInitInfoBaseHeaderKHR*>(&loader));code("init_loader",r);
 if(XR_FAILED(r))return finish("loader_init_failed");
 uint32_t count=0;r=xrEnumerateInstanceExtensionProperties(nullptr,0,&count,nullptr);code("extension_count_result",r);
 if(XR_FAILED(r)||count>10000)return finish("runtime_extension_query_failed");
 std::vector<XrExtensionProperties> extensions(count,{XR_TYPE_EXTENSION_PROPERTIES});
 r=xrEnumerateInstanceExtensionProperties(nullptr,count,&count,extensions.data());code("extension_list_result",r);
 if(XR_FAILED(r))return finish("runtime_extension_query_failed");
 bool face=false,android=false,headless=false,clockTime=false;
 json+="\"extensions\":[";
 for(uint32_t i=0;i<count;i++){
  if(i)json+=",";json+="\""+std::string(extensions[i].extensionName)+"\"";
  clockTime|=strcmp(extensions[i].extensionName,XR_KHR_CONVERT_TIMESPEC_TIME_EXTENSION_NAME)==0;
  headless|=strcmp(extensions[i].extensionName,XR_MND_HEADLESS_EXTENSION_NAME)==0;
  face|=strcmp(extensions[i].extensionName,XR_BD_FACIAL_SIMULATION_EXTENSION_NAME)==0;
  android|=strcmp(extensions[i].extensionName,XR_KHR_ANDROID_CREATE_INSTANCE_EXTENSION_NAME)==0;
 }
 json+="],\"facial_extension_advertised\":"+std::string(face?"true,":"false,");
 if(!face)return finish("facial_extension_not_advertised");
 std::vector<const char*> enabled{XR_BD_FACIAL_SIMULATION_EXTENSION_NAME};
 if(clockTime)enabled.push_back(XR_KHR_CONVERT_TIMESPEC_TIME_EXTENSION_NAME);
 if(headless)enabled.push_back(XR_MND_HEADLESS_EXTENSION_NAME);
 if(android)enabled.push_back(XR_KHR_ANDROID_CREATE_INSTANCE_EXTENSION_NAME);
 XrInstanceCreateInfoAndroidKHR androidInfo{XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR};androidInfo.applicationVM=vm;androidInfo.applicationActivity=activity;
 XrInstanceCreateInfo info{XR_TYPE_INSTANCE_CREATE_INFO};info.next=android?&androidInfo:nullptr;
 std::strcpy(info.applicationInfo.applicationName,"BlinkFaceSupportProbe");info.applicationInfo.applicationVersion=1;info.applicationInfo.apiVersion=XR_MAKE_VERSION(1,0,0);
 info.enabledExtensionCount=static_cast<uint32_t>(enabled.size());info.enabledExtensionNames=enabled.data();
 XrInstance instance=XR_NULL_HANDLE;r=xrCreateInstance(&info,&instance);code("create_instance",r);
 if(XR_FAILED(r))return finish("own_instance_creation_failed");
 XrSystemGetInfo sysInfo{XR_TYPE_SYSTEM_GET_INFO};sysInfo.formFactor=XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY;
 XrSystemId system=XR_NULL_SYSTEM_ID;r=xrGetSystem(instance,&sysInfo,&system);code("get_system",r);
 if(XR_FAILED(r)){code("destroy_instance",xrDestroyInstance(instance));return finish("system_query_failed");}
 XrSystemFacialSimulationPropertiesBD facial{XR_TYPE_SYSTEM_FACIAL_SIMULATION_PROPERTIES_BD};
 XrSystemProperties properties{XR_TYPE_SYSTEM_PROPERTIES};properties.next=&facial;
 r=xrGetSystemProperties(instance,system,&properties);code("get_system_properties",r);
 if(XR_SUCCEEDED(r))json+="\"supports_face_tracking\":"+std::string(facial.supportsFaceTracking?"true,":"false,");
 if(XR_SUCCEEDED(r)&&facial.supportsFaceTracking&&headless){
  XrSessionCreateInfo sessionInfo{XR_TYPE_SESSION_CREATE_INFO};sessionInfo.systemId=system;
  XrSession session=XR_NULL_HANDLE;
  auto sr=xrCreateSession(instance,&sessionInfo,&session);code("create_headless_session",sr);
  if(XR_SUCCEEDED(sr)){
   // Follow the public session lifecycle before querying sensors. Bounded to two seconds.
   bool begun=false;
   int lastState=0;
   for(int attempt=0;attempt<100;attempt++){
    XrEventDataBuffer event{XR_TYPE_EVENT_DATA_BUFFER};
    auto er=xrPollEvent(instance,&event);
    if(er==XR_SUCCESS&&event.type==XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED){
     auto state=reinterpret_cast<XrEventDataSessionStateChanged*>(&event);lastState=state->state;
     if(state->session==session&&state->state==XR_SESSION_STATE_READY){
      uint32_t viewCount=0;auto vr=xrEnumerateViewConfigurations(instance,system,0,&viewCount,nullptr);
      if(XR_SUCCEEDED(vr)&&viewCount>0&&viewCount<=32){
       std::vector<XrViewConfigurationType> views(viewCount);
       vr=xrEnumerateViewConfigurations(instance,system,viewCount,&viewCount,views.data());
       if(XR_SUCCEEDED(vr)){
        XrSessionBeginInfo begin{XR_TYPE_SESSION_BEGIN_INFO};begin.primaryViewConfigurationType=views[0];
        for(auto view:views)if(view==XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO)begin.primaryViewConfigurationType=view;
        auto br=xrBeginSession(session,&begin);code("begin_headless_session",br);begun=XR_SUCCEEDED(br);
       }
      }
      break;
     }
    }
    if(XR_FAILED(er))break;
    std::this_thread::sleep_for(std::chrono::milliseconds(20));
   }
   json+="\"last_session_state\":"+std::to_string(lastState)+",\"session_begun\":"+std::string(begun?"true,":"false,");
   PFN_xrEnumerateFacialSimulationModesBD enumerate=nullptr;
   auto pr=xrGetInstanceProcAddr(instance,"xrEnumerateFacialSimulationModesBD",reinterpret_cast<PFN_xrVoidFunction*>(&enumerate));code("get_enumerate_face_modes",pr);
   if(XR_SUCCEEDED(pr)&&enumerate){
    uint32_t modesCount=UINT32_MAX;auto mr=enumerate(session,0,&modesCount,nullptr);code("face_modes_count_result",mr);json+="\"face_modes_reported_count\":"+std::to_string(modesCount)+",";
    if(XR_SUCCEEDED(mr)&&modesCount<=64){
     std::vector<XrFacialSimulationModeBD> modes(modesCount);
     mr=enumerate(session,modesCount,&modesCount,modes.data());code("face_modes_list_result",mr);
     if(XR_SUCCEEDED(mr)){
      bool hasDefault=false;json+="\"face_modes\":[";
      for(uint32_t i=0;i<modesCount;i++){if(i)json+=",";json+=std::to_string(modes[i]);hasDefault|=modes[i]==XR_FACIAL_SIMULATION_MODE_DEFAULT_BD;}
      json+="],\"face_only_mode_advertised\":"+std::string(hasDefault?"true,":"false,");
     }
    }
   }
   // DEFAULT is a public enumerant. The spec requires FEATURE_UNSUPPORTED when unavailable.
   // Probe that return path even if mode enumeration was empty; never enable audio modes.
   if(faceAllowed&&begun){
    PFN_xrCreateFaceTrackerBD createFace=nullptr;
    PFN_xrDestroyFaceTrackerBD destroyFace=nullptr;
    PFN_xrGetFacialSimulationModeBD getMode=nullptr;
    PFN_xrGetFacialSimulationDataBD getData=nullptr;
    xrGetInstanceProcAddr(instance,"xrCreateFaceTrackerBD",reinterpret_cast<PFN_xrVoidFunction*>(&createFace));
    xrGetInstanceProcAddr(instance,"xrDestroyFaceTrackerBD",reinterpret_cast<PFN_xrVoidFunction*>(&destroyFace));
    xrGetInstanceProcAddr(instance,"xrGetFacialSimulationModeBD",reinterpret_cast<PFN_xrVoidFunction*>(&getMode));
    xrGetInstanceProcAddr(instance,"xrGetFacialSimulationDataBD",reinterpret_cast<PFN_xrVoidFunction*>(&getData));
    if(createFace&&destroyFace&&getMode&&getData){
     XrFaceTrackerCreateInfoBD faceInfo{XR_TYPE_FACE_TRACKER_CREATE_INFO_BD};faceInfo.mode=XR_FACIAL_SIMULATION_MODE_DEFAULT_BD;
     XrFaceTrackerBD tracker=XR_NULL_HANDLE;auto fr=createFace(session,&faceInfo,&tracker);code("create_default_face_tracker",fr);
     json+="\"face_tracker_handle_written\":"+std::string(tracker!=XR_NULL_HANDLE?"true,":"false,");
     if(XR_SUCCEEDED(fr)&&tracker!=XR_NULL_HANDLE){
      XrFacialSimulationModeBD mode=XR_FACIAL_SIMULATION_MODE_MAX_ENUM_BD;
      auto gr=getMode(tracker,&mode);code("get_face_mode",gr);json+="\"reported_face_mode\":"+std::to_string(mode)+",";
      if(XR_SUCCEEDED(gr)&&mode==XR_FACIAL_SIMULATION_MODE_DEFAULT_BD&&clockTime){
       PFN_xrConvertTimespecTimeToTimeKHR convert=nullptr;
       xrGetInstanceProcAddr(instance,"xrConvertTimespecTimeToTimeKHR",reinterpret_cast<PFN_xrVoidFunction*>(&convert));
       if(convert){
        timespec now{};clock_gettime(CLOCK_MONOTONIC,&now);XrTime xrNow=0;
        auto tr=convert(instance,&now,&xrNow);code("convert_monotonic_time",tr);
        if(XR_SUCCEEDED(tr)&&xrNow>0){
         std::vector<float> weights(XR_FACE_EXPRESSION_COUNT_BD,std::numeric_limits<float>::quiet_NaN());
         XrFacialSimulationDataGetInfoBD sampleInfo{XR_TYPE_FACIAL_SIMULATION_DATA_GET_INFO_BD};sampleInfo.time=xrNow;
         XrFacialSimulationDataBD data{XR_TYPE_FACIAL_SIMULATION_DATA_BD};data.faceExpressionWeightCount=weights.size();data.faceExpressionWeights=weights.data();
         auto dr=getData(tracker,&sampleInfo,&data);code("get_face_sample",dr);
         uint32_t finiteCount=0;for(auto value:weights)if(std::isfinite(value))finiteCount++;
         json+="\"sample_finite_weights\":"+std::to_string(finiteCount)+",\"upper_face_valid\":"+std::string(data.isUpperFaceDataValid?"true,":"false,")+"\"sample_time_written\":"+std::string(data.time>0?"true,":"false,");
        }
       }
      }
      code("destroy_face_tracker",destroyFace(tracker));
     }
    }
   }
   code("destroy_headless_session",xrDestroySession(session));
  }
 }
 code("destroy_instance",xrDestroyInstance(instance));
 return finish(XR_SUCCEEDED(r)?"diagnostic_query_complete_no_training_decision":"system_properties_query_failed");
}
