const svgAvatar = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" fill="none">
  <rect width="200" height="200" rx="100" fill="#e2e8f0"/>
  <circle cx="100" cy="80" r="40" fill="#94a3b8"/>
  <ellipse cx="100" cy="180" rx="70" ry="50" fill="#94a3b8"/>
</svg>`

const svgPlaceholder = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 400" fill="none">
  <rect width="600" height="400" fill="#f1f5f9"/>
  <path d="M240 180h120v40H240z" fill="#cbd5e1"/>
  <circle cx="300" cy="160" r="50" fill="#cbd5e1"/>
</svg>`

export const DEFAULT_AVATAR_URL = `data:image/svg+xml,${encodeURIComponent(svgAvatar)}`
export const DEFAULT_TASK_IMAGE = `data:image/svg+xml,${encodeURIComponent(svgPlaceholder)}`
