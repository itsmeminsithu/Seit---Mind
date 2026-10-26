/** Web & audio use the same origin. Next proxies /api/* to the shared FastAPI service. */
const api = (process.env.MIND_API_SERVER_URL || 'http://127.0.0.1:8000').replace(/\/$/, '');
const nextConfig = {
  async rewrites() {
    return [{source: '/api/:path*', destination: `${api}/api/:path*`},
            {source: '/health',destination: `${api}/health`}];
  },
};
export default nextConfig;
