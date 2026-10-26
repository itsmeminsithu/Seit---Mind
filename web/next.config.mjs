// Web & audio use the same origin. Next proxies /api/* to the shared FastAPI service.
// When GITHUB_PAGES=true, build a static export for GitHub Pages (no server, no /api proxy).
// The Pages workflow copies audio/*.mp3 into web/public/audio/ before building, and
// page.tsx falls back to those static audio files when /api/sessions is unavailable.
const api = (process.env.MIND_API_SERVER_URL || 'http://127.0.0.1:8000').replace(/\/$/, '');
const isPages = process.env.GITHUB_PAGES === 'true';
const nextConfig = {
  ...(isPages ? { output: 'export', basePath: '/Seit---Mind' } : {}),
  async rewrites() {
    if (isPages) return [];
    return [{source: '/api/:path*', destination: `${api}/api/:path*`},
            {source: '/health',destination: `${api}/health`}];
  },
};
export default nextConfig;
