import { profileImageUrl } from "@/lib/profileImage";

type AvatarProps = {
  src: string | null | undefined;
  alt: string;
  size?: number;
  className?: string;
};

export function Avatar({ src, alt, size = 40, className = "" }: AvatarProps) {
  return (
    // eslint-disable-next-line @next/next/no-img-element -- 외부 URL·data URL·로컬 SVG 혼용
    <img
      src={profileImageUrl(src)}
      alt={alt}
      width={size}
      height={size}
      className={`shrink-0 rounded-full border border-neutral-200 bg-neutral-100 object-cover ${className}`}
    />
  );
}
