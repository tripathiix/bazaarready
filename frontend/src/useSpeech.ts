import { useRef, useState } from "react";

// The browser's built-in speech recognition (Chrome and Edge). It turns speech into text on the
// device's side, so the server only ever receives text.
type Recognition = {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  onresult: ((event: { resultIndex: number; results: ArrayLike<ArrayLike<{ transcript: string }> & { isFinal: boolean }> }) => void) | null;
  onend: (() => void) | null;
  onerror: (() => void) | null;
  start: () => void;
  stop: () => void;
};

type RecognitionConstructor = new () => Recognition;

function recognitionConstructor(): RecognitionConstructor | undefined {
  const w = window as unknown as { SpeechRecognition?: RecognitionConstructor; webkitSpeechRecognition?: RecognitionConstructor };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition;
}

// Listening stops by itself once the speaker goes quiet: after a short pause following speech,
// or if nothing is said at all for a while. Tapping the button still stops it at any time.
const PAUSE_MS = 2200;
const NOTHING_SAID_MS = 8000;

export function useSpeech(onText: (text: string) => void) {
  const [listening, setListening] = useState(false);
  const recognition = useRef<Recognition | null>(null);
  const silence = useRef<number | undefined>(undefined);
  const supported = recognitionConstructor() !== undefined;

  function waitForQuiet(ms: number) {
    window.clearTimeout(silence.current);
    silence.current = window.setTimeout(() => recognition.current?.stop(), ms);
  }

  function start(lang: string) {
    const Ctor = recognitionConstructor();
    if (!Ctor) return;
    const r = new Ctor();
    r.lang = lang;
    r.continuous = true;
    r.interimResults = true; // partial words show the person is still talking
    r.onresult = (event) => {
      for (let i = event.resultIndex; i < event.results.length; i++) {
        if (event.results[i].isFinal) onText(event.results[i][0].transcript.trim());
      }
      waitForQuiet(PAUSE_MS);
    };
    r.onend = () => {
      window.clearTimeout(silence.current);
      setListening(false);
    };
    r.onerror = () => {
      window.clearTimeout(silence.current);
      setListening(false);
    };
    recognition.current = r;
    r.start();
    setListening(true);
    waitForQuiet(NOTHING_SAID_MS);
  }

  function stop() {
    window.clearTimeout(silence.current);
    recognition.current?.stop();
    setListening(false);
  }

  return { supported, listening, start, stop };
}

/** Reads a piece of text aloud in the given language, if the device has a voice for it. */
export function speakAloud(text: string, lang: string) {
  if (!("speechSynthesis" in window) || !text) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = lang;
  window.speechSynthesis.speak(utterance);
}

export const canSpeakAloud = () => "speechSynthesis" in window;
