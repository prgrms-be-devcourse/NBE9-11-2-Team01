"use client";

/** 정적 레이어만 사용 — 대형 filter: blur + 무한 애니메이션은 GPU 부담이 커서 제거 */
export function MeshBackground() {
  return (
    <div
      className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-white"
      aria-hidden
    >
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_120%_80%_at_50%_-25%,rgb(245,245,245),transparent_55%)]" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_90%_55%_at_100%_0%,rgb(238,238,238),transparent_48%)] opacity-90" />
      <div
        className="absolute inset-0 opacity-[0.2]"
        style={{
          backgroundImage: `linear-gradient(rgba(0,0,0,.05) 1px, transparent 1px),
            linear-gradient(90deg, rgba(0,0,0,.05) 1px, transparent 1px)`,
          backgroundSize: "64px 64px",
        }}
      />
    </div>
  );
}
