#!/bin/sh
set -e

# Set default values if environment variables are not provided
export BACKEND_HOST=${BACKEND_HOST:-backend}
export BACKEND_PORT=${BACKEND_PORT:-8080}
export FRONTEND_PORT=${FRONTEND_PORT:-80}

echo "Starting Nginx with configuration:"
echo "  BACKEND_HOST: $BACKEND_HOST"
echo "  BACKEND_PORT: $BACKEND_PORT"
echo "  FRONTEND_PORT: $FRONTEND_PORT"

# Replace environment variables in nginx config template
envsubst '${BACKEND_HOST} ${BACKEND_PORT} ${FRONTEND_PORT}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Test nginx configuration
nginx -t

# Execute the CMD (start nginx)
exec "$@"