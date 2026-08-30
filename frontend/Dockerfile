# syntax=docker/dockerfile:1

# --- Stage 1 : build Angular ---
FROM node:24-alpine AS build
WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npx ng build --configuration production

# --- Stage 2 : runtime nginx ---
FROM nginx:1.31-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=5 \
    CMD wget -q -O- http://localhost/ || exit 1
