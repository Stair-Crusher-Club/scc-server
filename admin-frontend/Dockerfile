FROM nginx

WORKDIR /app
ADD build /app/build

RUN rm /etc/nginx/conf.d/default.conf
ADD nginx.conf /etc/nginx/conf.d

EXPOSE 80
