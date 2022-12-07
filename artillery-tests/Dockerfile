FROM node:16-alpine as builder

WORKDIR /app

# install git
RUN apk add --no-cache git

RUN npm install \ 
artillery \ 
@faker-js/faker \
node-fetch \ 
https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git

FROM node:16-alpine

WORKDIR /app

# ENV TARGET_URL 

# copy from builder
COPY --from=builder /app/node_modules /app/node_modules

ADD images /app/images
ADD test-utils.js /app
ADD workload0.yml /app

CMD npx artillery run --output workload0.json workload0.yml ; \
  npx artillery report --output /aci/logs/workload0.html workload0.json;