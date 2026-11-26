package com.example.blinkisland.capabilityprobe

/** Availability metrics only: no gaze coordinates, inferred eyelid state or training success. */
class EyeSignalWindow {
 private var received=0
 private var duplicates=0
 private var backwards=0
 private var invalid=0
 private var unchanged=0
 private var lastTimestamp:Long?=null
 private var lastArrival:Long?=null
 private var maxGap=0L
 fun sample(sourceTimestamp:Long,arrivalMs:Long,finitePose:Boolean,samePose:Boolean){
  received++
  lastTimestamp?.let{if(sourceTimestamp==it)duplicates++ else if(sourceTimestamp<it)backwards++}
  lastArrival?.let{maxGap=maxOf(maxGap,(arrivalMs-it).coerceAtLeast(0))}
  if(!finitePose)invalid++
  if(samePose)unchanged++
  lastTimestamp=sourceTimestamp;lastArrival=arrivalMs
 }
 fun snapshot(nowMs:Long):String {
  val age=lastArrival?.let{(nowMs-it).coerceAtLeast(0)}?:-1
  val value="received=$received repeated_timestamp=$duplicates reversed_timestamp=$backwards nonfinite_pose=$invalid unchanged_pose=$unchanged max_arrival_gap_ms=$maxGap since_arrival_ms=$age"
  received=0;duplicates=0;backwards=0;invalid=0;unchanged=0;maxGap=0
  return value
 }
}
