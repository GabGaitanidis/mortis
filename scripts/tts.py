from piper.voice import PiperVoice
import wave
import subprocess
import sys
import os
from pathlib import Path
from env_utils import load_env

load_env()

voice = PiperVoice.load(os.getenv("MORTIS_PIPER_VOICE_PATH", str(Path.home() / "piper-voices" / "en_US-lessac-medium.onnx")))
text = ""

OUTPUT = os.getenv("MORTIS_ANSWER_TEXT_PATH", str(Path.home() / "mortisAnswer.txt"))
INPUT  = os.getenv("MORTIS_ANSWER_AUDIO_PATH", str(Path.home() / "mortisAnswer.wav"))
with open(OUTPUT) as f:
    text = f.read()


if not text:
    sys.exit(0)

with wave.open(INPUT, "wb") as wav_file:
    wav_file.setnchannels(1)
    wav_file.setsampwidth(2) 
    wav_file.setframerate(voice.config.sample_rate)

    for chunk in voice.synthesize(text):
        wav_file.writeframes(chunk.audio_int16_bytes)


subprocess.run(["paplay", INPUT])