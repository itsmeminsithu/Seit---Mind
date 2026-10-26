FROM node:22-alpine
WORKDIR /app
COPY web/package*.json ./
RUN npm install --no-audit --no-fund
COPY web/ ./
ARG MIND_API_SERVER_URL=http://backend:8000
ENV MIND_API_SERVER_URL=$MIND_API_SERVER_URL
RUN npm run build
ENV NODE_ENV=production
EXPOSE 3000
CMD ["npm", "run", "start"]
