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

# COPY images ./images
# COPY test-utils.js .
# COPY workload0.yml .
# COPY workload1.yml .
# COPY workload2.yml .

CMD npx artillery run --output docker0.json workload0.yml ; \
  npx artillery report docker0.json; \
  npx artillery run --output docker1.json workload1.yml ; \
  npx artillery report docker1.json; \
  npx artillery run --output docker2.json workload2.yml ; \
  npx artillery report docker2.json;