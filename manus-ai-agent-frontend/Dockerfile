# front end build phase
FROM node:20-alpine AS build
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# Run phase - Use nginx to host static files
FROM nginx:alpine
# Copy the build product to the nginx static file directory
COPY --from=build /app/dist /usr/share/nginx/html
# Copy the custom nginx configuration to replace the default configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Expose ports
EXPOSE 80
# Start Nginx
CMD ["nginx", "-g", "daemon off;"] 