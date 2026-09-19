Drop your recorded clips here as plain files (no subfolders needed), named:

  run.mp3        - generic "Run" cue, used whenever the specific duration file below is missing
  walk.mp3       - generic "Walk" cue (also used for the warmup, which is just a walk)
  cooldown.mp3   - played when the last interval ends and the 5 min cooldown starts
  complete.mp3   - played when the whole session (incl. cooldown) is done

Once you start recording duration-specific clips, name them "<type>_<seconds>.mp3",
e.g. run_180.mp3 ("Run for 3 minutes") or walk_90.mp3 ("Walk one and a half minute").
The app already looks up each interval's specific file first (see the "sound" field
in programs.json) and only falls back to the generic run.mp3/walk.mp3 if that file
isn't there yet — so you can add these gradually without touching any code.
