"""Execute an inspected, single-candidate Tripo plan. Never resubmit a started asset."""
from pathlib import Path
import argparse
import json
import os
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / 'review/tripo-storybook-2026-11-27'

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('ids', nargs='+')
    args = parser.parse_args()
    plan = json.loads((FOLDER / 'generation-plan.json').read_text())
    if plan['estimatedCredits'] > 360 or len(plan['items']) > 6:
        raise RuntimeError('Plan exceeds the declared six-candidate/360-credit scope')
    runs = FOLDER / 'runs'
    runs.mkdir(exist_ok=True)
    for asset_id in args.ids:
        item = next(i for i in plan['items'] if i['id'] == asset_id)
        marker = runs / (asset_id + '.started.json')
        if marker.exists():
            raise RuntimeError(asset_id + ': already started; recover the task instead of resubmitting')
        command = item['command']
        if command[:2] != ['tripo', 'make'] or '--dry-run' in command:
            raise RuntimeError('Unexpected submission command')
        with marker.open('x') as f:
            json.dump({'asset': asset_id, 'startedAtUnix': time.time(), 'maxCandidates': 1, 'noPaidRetry': True}, f)
        print('Submitting ' + asset_id, flush=True)
        with (runs / (asset_id + '.stdout.json')).open('w') as stdout, (runs / (asset_id + '.stderr.log')).open('w') as stderr:
            result = subprocess.run(command, cwd=ROOT, env={**os.environ, 'TRIPO_CLIENT_APP': 'codex'}, stdout=stdout, stderr=stderr)
        raw = (runs / (asset_id + '.stdout.json')).read_text()
        try:
            data = json.loads(raw)
        except ValueError:
            data = {'resultParseFailed': True}
        safe = {k: data[k] for k in ('task_id', 'credits_consumed', 'model_file', 'preview', 'status') if k in data}
        safe.update(asset=asset_id, exitCode=result.returncode)
        (runs / (asset_id + '.receipt.json')).write_text(json.dumps(safe, ensure_ascii=False, indent=2) + '\n')
        print(json.dumps({k: v for k, v in safe.items() if k != 'preview'}, ensure_ascii=False), flush=True)
        if result.returncode:
            raise RuntimeError(asset_id + ': CLI stopped; inspect and recover without another submission')
        if data.get('credits_consumed', 0) > item['dryRun']['generation_credits_estimate']:
            raise RuntimeError(asset_id + ': fee exceeded estimate; stop further assets')

if __name__ == '__main__':
    main()
