// 3D tilt: cards lean toward the mouse with a soft light where the pointer is. Only on devices with a
// mouse, and never for people who turn motion off. Works for elements React adds later too.
export function installTilt(selector: string) {
  if (typeof window === "undefined") return;
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
  if (!window.matchMedia("(hover: hover) and (pointer: fine)").matches) return;
  let current: HTMLElement | null = null;

  function reset(el: HTMLElement) {
    el.classList.remove("tilting");
    el.style.removeProperty("--rx");
    el.style.removeProperty("--ry");
  }

  document.addEventListener(
    "pointermove",
    (e) => {
      const target = e.target instanceof Element ? (e.target.closest(selector) as HTMLElement | null) : null;
      if (current && current !== target) reset(current);
      current = target;
      if (!target) return;
      const r = target.getBoundingClientRect();
      const x = (e.clientX - r.left) / r.width;
      const y = (e.clientY - r.top) / r.height;
      target.style.setProperty("--ry", `${((x - 0.5) * 12).toFixed(2)}deg`);
      target.style.setProperty("--rx", `${((0.5 - y) * 9).toFixed(2)}deg`);
      target.style.setProperty("--mx", `${(x * 100).toFixed(1)}%`);
      target.style.setProperty("--my", `${(y * 100).toFixed(1)}%`);
      target.classList.add("tilting");
    },
    { passive: true },
  );
  document.addEventListener("pointerleave", () => current && reset(current));
}
