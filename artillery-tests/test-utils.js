const { faker } = require("@faker-js/faker");
const fs = require("fs");
const path = require("path");

let imagesIds = [];
let images = [];
let users = [];
let auctions = [];
let bids = [];

/**
 *  All endpoints starting with the following prefixes
 *  will be aggregated in the same for the statistics
 */
let statsPrefix = [
  ["/rest/media/", "GET"],
  ["/rest/media", "POST"],
  ["/rest/user", "POST"],
  ["/rest/user/auth", "POST"],
  ["/rest/auction", "POST"],
];

/*****************************************************
 **************** AUXILIARY FUNCTIONS ****************
 *****************************************************/

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
  let basedir;
  if (fs.existsSync("/images")) basedir = "/images";
  else basedir = "images";
  fs.readdirSync(basedir).forEach((file) => {
    if (path.extname(file) === ".jpeg") {
      var img = fs.readFileSync(basedir + "/" + file);
      images.push(img);
    }
  });

  let str;
  if (fs.existsSync("users.data")) {
    str = fs.readFileSync("users.data");
    users = JSON.parse(str);
  }
  if (fs.existsSync("auctions.data")) {
    str = fs.readFileSync("auctions.data");
    auctions = JSON.parse(str);
  }
}

loadData();

/*****************************************************
 *************** ARTILLERY FUNCTIONS *****************
 *****************************************************/

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
  requestParams.body = images.sample();
  return next();
}

/*****************************************************
 ******************** GENERATORS *********************
 *****************************************************/

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
 * Generate data for a new auction using Faker
 */
function genNewAuction(context, events, done) {
  context.vars.title = `${faker.commerce.productName()}`;
  context.vars.description = `${faker.commerce.productDescription()}`;
  context.vars.minPrice = `${faker.commerce.price(5, 50)}`;
  context.vars.endTime = "2022-12-07T21:22:35.328472158Z[Europe/Lisbon]";
  return done();
}

/*****************************************************
 ****************** REPLY PROCESSORS *****************
 *****************************************************/

/**
 * Process reply of the download of an image.
 * Update the next image to read.
 * Write the image id to disk.
 */
function processUploadImageReply(requestParams, response, context, ee, next) {
  if (typeof response.body !== "undefined" && response.body.length > 0) {
    imagesIds.push(response.body);
    fs.writeFileSync("images.data", JSON.stringify(imagesIds));
  }
  return next();
}

/**
 * Process reply of newly created users to store the user object
 * on file with the input password.
 */
function processNewUserReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let u = JSON.parse(response.body);
    u.pwd = context.vars.pwd;
    users.push(u);
    fs.writeFileSync("users.data", JSON.stringify(users));
  }
  return next();
}

/**
 * Process reply of new auctions and store the body response
 * on file and on write on disk
 */
function processNewAuctionReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let a = JSON.parse(response.body);
    auctions.push(a);
    fs.writeFileSync("auctions.data", JSON.stringify(auctions));
  } else {
    console.log(response.statusCode + " " + response.body);
  }
  return next();
}

/**
 * Process reply of login and store the cookie
 */
function processLoginReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let cookie = context.vars.cookie;
    let c = cookie[0];
    //save mycookie on disk
    fs.writeFileSync("cookie.data", JSON.stringify(c));
  }
  return next();
}

/**
 * Process reply of auction bid and store the object on disk
 */
function processNewBidReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let b = JSON.parse(response.body);
    bids.push(b);
    fs.writeFileSync("bids.data", JSON.stringify(bids));

    //update auction on disk
    let a = auctions.find((a) => a.id == b.auctionId);
    let tmp = a;
    a.winnerBid = b.value;
    auctions.splice(auctions.indexOf(tmp), 1, a);
    fs.writeFileSync("auctions.data", JSON.stringify(auctions));
  }
  return next();
}

/*****************************************************
 ********************** SELECTORS ********************
 *****************************************************/

/**
 * Select image id
 * if imageIds is empty fetch from disk
 */
function selectImageId(context, events, done) {
  if (imagesIds.length > 0) {
    context.vars.imageId = imagesIds.sample();
  } else {
    imagesIds = JSON.parse(fs.readFileSync("images.data"));
    context.vars.imageId = imagesIds.sample();
  }
  return done();
}

/**
 * Select a user to login
 * If users is empty fetch from disk
 */
function selectUserToLogin(context, events, done) {
  if (users.length > 0) {
    let u = users.sample();
    context.vars.nickname = u.nickname;
    context.vars.pwd = u.pwd;
  } else {
    users = JSON.parse(fs.readFileSync("users.data"));
    let u = users.sample();
    context.vars.nickname = u.nickname;
    context.vars.pwd = u.pwd;
  }
  return done();
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
 * Select an auction and a user to bid.
 * If the user is the owner of the auction, select another user.
 */
function selectAuctionAndUserToBid(context, events, done) {
  let a, u;

  //Get the auction
  if (auctions.length > 0) {
    a = auctions.sample();
  }

  context.vars.auctionId = a.id;

  if (a.winnerBid == null) {
    context.vars.value = a.minPrice + 1;
  } else {
    context.vars.value = a.winnerBid + 1;
  }

  //Get the user who's bidding
  if (users.length > 0) {
    u = users.sample();
    while (u.nickname == a.ownerNickname) {
      u = users.sample();
    }
  }

  context.vars.userNickname = u.nickname;
  context.vars.userPwd = u.pwd;

  return done();
}

module.exports = {
  uploadImageBody,
  genNewUser,
  genNewAuction,
  processUploadImageReply,
  processNewUserReply,
  processNewAuctionReply,
  processLoginReply,
  processNewBidReply,
  selectImageId,
  selectUserToLogin,
  selectImageToDownload,
  selectAuctionAndUserToBid,
};
