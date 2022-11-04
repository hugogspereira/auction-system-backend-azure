const { faker } = require("@faker-js/faker");
const fs = require("fs");
const path = require("path");

let imagesIds = [];
let images = [];
let users = [];

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
let statsPrefix = [
  ["/rest/media/", "GET"],
  ["/rest/media", "POST"],
  ["/rest/user", "POST"],
];

// Function used to compress statistics
global.myProcessEndpoint = function (str, method) {
  var i = 0;
  for (i = 0; i < statsPrefix.length; i++) {
    if (str.startsWith(statsPrefix[i][0]) && method == statsPrefix[i][1])
      return method + ":" + statsPrefix[i][0];
  }
  return method + ":" + str;
};

// Auxiliary function to select an element from an array
Array.prototype.sample = function () {
  return this[Math.floor(Math.random() * this.length)];
};

// Returns a random value, from 0 to val
function random(val) {
  return Math.floor(Math.random() * val);
}

// Loads data about images from disk
function loadData() {
  var basedir;
  if (fs.existsSync("/images")) basedir = "/images";
  else basedir = "images";
  fs.readdirSync(basedir).forEach((file) => {
    if (path.extname(file) === ".jpeg") {
      var img = fs.readFileSync(basedir + "/" + file);
      images.push(img);
    }
  });
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
  requestParams.body = images.sample();
  return next();
}

/**
 * Process reply of the download of an image.
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
  if (typeof response.body !== "undefined" && response.body.length > 0) {
    imagesIds.push(response.body);
  }
  return next();
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
  if (imagesIds.length > 0) {
    context.vars.imageId = imagesIds.sample();
  } else {
    delete context.vars.imageId;
  }
  return done();
}

/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
  const first = `${faker.name.firstName()}`;
  const last = `${faker.name.lastName()}`;
  context.vars.nickname = first + "." + last + "." + Date.now();
  context.vars.name = first + " " + last;
  context.vars.pwd = `${faker.internet.password()}`;
  return done();
}

/**
 * Process reply for of new users to store the id on file
 */
function processNewUserReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let u = JSON.parse(response.body);
    users.push(u);
    fs.writeFileSync("users.data", JSON.stringify(users));
  }
  return next();
}

/**
 * Select a user to login
 * If no users are available, create a new one
 * If no users are available and the endpoint is not for creating a new user, skip the request
 */
function selectUserToLogin(context, events, done) {
  if (users.length > 0) {
    let u = users.sample();
    context.vars.nickname = u.nickname;
    context.vars.pwd = u.password;
  } else {
    if (context.vars.path.startsWith("/rest/user")) {
      genNewUser(context, events, done);
    } else {
      context.vars.skip = true;
    }
  }
  return done();
}

/**
 * Select an image
 */

/**
 * Generate data for a new auction using Faker
 */
function genNewAuction(context, events, done) {
  context.vars.title = `${faker.commerce.productName()}`;
  context.vars.description = `${faker.commerce.productDescription()}`;
  context.vars.minPrice = `${faker.commerce.price(5, 50)}`;
  context.vars.endTime = `${faker.date.future()}`;
  return done();
}

module.exports = {
  uploadImageBody,
  processUploadReply,
  selectImageToDownload,
  genNewUser,
  processNewUserReply,
  selectUserToLogin,
  genNewAuction,
};
