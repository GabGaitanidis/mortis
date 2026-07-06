import os
import sys
from openwakeword.model import Model
import sounddevice as sd

os.environ["OMP_NUM_THREADS"] = "1"

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
                detected = True
                raise sd.CallbackStop

    try:
        with sd.InputStream(samplerate=samplerate, channels=1, dtype="int16",
                             blocksize=blocksize, callback=callback):
            while not detected:
                sd.sleep(100)
    except sd.CallbackStop:
        pass
    sd.stop()

if __name__ == "__main__":
    wait_for_wake_word()
    print("detected", flush=True)
    sys.exit(0)