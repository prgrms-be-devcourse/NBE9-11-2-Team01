"use client";

/** 정적 레이어만 사용 — 대형 filter: blur + 무한 애니메이션은 GPU 부담이 커서 제거 */
export function MeshBackground() {
  return (
    <div
      className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-white"
      aria-hidden
    >
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_120%_80%_at_50%_-25%,rgb(239,246,255),transparent_56%)]" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_95%_60%_at_100%_0%,rgb(219,234,254),transparent_50%)] opacity-90" />
      <div
        className="absolute inset-0 opacity-[0.18]"
        style={{
          backgroundImage: `linear-gradient(rgba(107,114,128,.09) 1px, transparent 1px),
            linear-gradient(90deg, rgba(107,114,128,.09) 1px, transparent 1px)`,
          backgroundSize: "64px 64px",
        }}
      />
    </div>
  );
}
