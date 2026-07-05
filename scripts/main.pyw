import subprocess
from openwakeword.model import Model
import sounddevice as sd
import os
from pathlib import Path

from env_utils import load_env

load_env()

os.environ["OMP_NUM_THREADS"] = "1"

def run_command_visible(command: list[str]):
    subprocess.run(command)


def wait_for_wake_word():
    model = Model()
    samplerate = 16000
    blocksize = 4000
    detected = False

    def callback(indata, frames, time, status):
        nonlocal detected
        audio = indata[:, 0]
        prediction = model.predict(audio)
        for wakeword, score in prediction.items():
            if score > 0.5 and wakeword == "hey_jarvis":
                print("wake word detected")
                detected = True
                raise sd.CallbackStop

    try:
        with sd.InputStream(
                samplerate=samplerate,
                channels=1,
                dtype="int16",
                blocksize=blocksize,
                callback=callback):
            while not detected:
                sd.sleep(100)
    except sd.CallbackStop:
        pass
    sd.stop()

while True:
    
    wait_for_wake_word()
    run_command_visible([
        "java", "-jar", os.getenv("MORTIS_JAR_PATH", str(Path(__file__).resolve().parent.parent / "target" / "mortis-1.0-SNAPSHOT-jar-with-dependencies.jar"))
    ])