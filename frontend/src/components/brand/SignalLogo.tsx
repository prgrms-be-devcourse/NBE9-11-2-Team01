type SignalLogoProps = {
  size?: number;
  className?: string;
};

/** 합격시그널 브랜드 마크 (public/signal-logo.svg) */
export function SignalLogo({
  size = 36,
  className = "",
}: SignalLogoProps) {
  return (
    // eslint-disable-next-line @next/next/no-img-element -- 정적 SVG 자산
    <img
      src="/signal-logo.svg"
      alt="합격시그널"
      width={size}
      height={size}
      className={className}
    />
  );
}
