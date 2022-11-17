const { faker } = require("@faker-js/faker");
const fs = require("fs");
const path = require("path");

let imagesIds = [];
let images = [];
let users = [];
let auctions = [];
let bids = [];
let questions = [];

//TODO: update SELECTORS to no longer fetch from disk

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
  ["/rest/*/bid", "POST"],
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

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function () {
  return this[randomSkewed(this.length)];
};

// Returns a random value, from 0 to val
function random(val) {
  return Math.floor(Math.random() * val);
}

// Returns a random value, from 0 to val
function randomSkewed(val) {
  let beta = Math.pow(Math.sin((Math.random() * Math.PI) / 2), 2);
  let beta_left = beta < 0.5 ? 2 * beta : 2 * (1 - beta);
  return Math.floor(beta_left * val);
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
  if (fs.existsSync("images.data")) {
    str = fs.readFileSync("images.data");
    imagesIds = JSON.parse(str);
  }
  if (fs.existsSync("bids.data")) {
    str = fs.readFileSync("bids.data");
    bids = JSON.parse(str);
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
  // context.vars.endTime = "2022-12-07T21:22:35.328472158Z[Europe/Lisbon]";
  context.vars.endTime = `${faker.date.future().toISOString()}`;
  return done();
}

/**
 * Generate data for the update of an user
 */
function genUserUpdate(context, events, done) {
  context.vars.name = `${faker.name.firstName()} ${faker.name.lastName()}`;

  let user = users.find((u) => u.nickname === context.vars.nickname);
  context.vars.photoId = user.photoId;

  return done();
}

/**
 * Generate data for a new bid
 */
function genNewBid(context, events, done) {
  if (typeof context.vars.bidValue == "undefined") {
    if (typeof context.vars.minimumPrice == "undefined") {
      context.vars.bidValue = random(100);
    } else {
      context.vars.bidValue = context.vars.minimumPrice + random(3);
    }
  }
  context.vars.value = context.vars.bidValue;
  context.vars.bidValue = context.vars.bidValue + 1 + random(3);
  return done();
}

/**
 * Generate data for a new question
 */
function genNewQuestion(context, events, done) {
  context.vars.question = `${faker.lorem.sentence()}`;
  return done();
}

/**
 * Generate data for a new reply
 */
function genNewReply(context, events, done) {
  context.vars.message = `${faker.lorem.sentence()}`;
  context.vars.reply = true;
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
    let a = auctions.find((a) => a.id === b.auctionId);
    let tmp = a;
    a.winnerBid = b.value;
    auctions.splice(auctions.indexOf(tmp), 1, a);
    fs.writeFileSync("auctions.data", JSON.stringify(auctions));
  }
  return next();
}

/**
 * Process reply of questions and store the object on disk
 */
function processNewQuestionReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let q = JSON.parse(response.body);
    questions.push(q);
    fs.writeFileSync("questions.data", JSON.stringify(questions));
  }
  return next();
}

/**
 * Process reply of user deletion and remove the user from disk.
 */
function processDeleteUserReply(requestParams, response, context, ee, next) {
  if (response.statusCode >= 200 && response.statusCode < 300) {
    let u = users.find((u) => u.nickname === context.vars.userNickname);
    users.splice(users.indexOf(u), 1);
    fs.writeFileSync("users.data", JSON.stringify(users));
  }
  return next();
}

/**
 * Process reply of user update and update the user in disk.
 */
function processUpdateUserReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let u = JSON.parse(response.body);
    let tmp = users.find((u) => u.nickname === context.vars.nickname);
    users.splice(users.indexOf(tmp), 1, u);
    fs.writeFileSync("users.data", JSON.stringify(users));
  }
  return next();
}

/**
 * Process reply of auction search
 * Choose one auction id from the array and save in context
 */
function processSearchReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let a = JSON.parse(response.body);
    if (a.length > 0) {
      let auction = a[random(a.length - 1)];
      context.vars.auctionId = auction.id;
    }
  }
  return next();
}

/**
 * Process reply of list of auctions and select one
 */
function processListAuctionsReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let a = JSON.parse(response.body);
    if (a.length > 0) {
      let auction = a[random(a.length - 1)];
      context.vars.auctionId = auction.id;
    }
  }
  return next();
}

/**
 * Process reply of list of questions and select one
 */
function processListQuestionsReply(requestParams, response, context, ee, next) {
  if (
    response.statusCode >= 200 &&
    response.statusCode < 300 &&
    response.body.length > 0
  ) {
    let q = JSON.parse(response.body);
    if (q.length > 0) {
      let question = q[random(q.length - 1)];
      context.vars.questionId = question.questionId;
    }
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
 * Select user
 */
function selectUserSkewed(context, events, done) {
  if (users.length > 0) {
    let user = users.sampleSkewed();
    context.vars.nickname = user.nickname;
    context.vars.pwd = user.pwd;
  } else {
    delete context.vars.nickname;
    delete context.vars.pwd;
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

/**
 * Select an auction and a user to ask a question.
 * If the user is the owner of the auction, select another user.
 */
function selectUserToAskQuestion(context, events, done) {
  let a, u;

  //Get the auction
  if (auctions.length > 0) {
    a = auctions.sample();
  }

  context.vars.auctionId = a.id;

  //Get the user who's asking the question
  if (users.length > 0) {
    u = users.sample();
    while (u.nickname === a.ownerNickname) {
      u = users.sample();
    }
  }

  context.vars.userNickname = u.nickname;
  context.vars.userPwd = u.pwd;
  context.vars.question = faker.lorem.sentence();

  return done();
}

/**
 * Select owner of the auctionId in context to give a reply.
 */
function selectOwnerToReply(context, events, done) {
  let a, u;

  //Get the auction
  a = auctions.find((a) => a.id === context.vars.auctionId);

  //Get the user who's replying the question
  if (users.length > 0) {
    u = users.find((u) => u.nickname === a.ownerNickname);
  }

  context.vars.ownerNickname = u.nickname;
  context.vars.ownerPwd = u.pwd;
  context.vars.replyMessage = faker.lorem.sentence();
  context.vars.reply = true;

  return done();
}

/**
 * Select the password of a specific user
 */
function selectUserPwd(context, events, done) {
  let u = users.find((u) => u.nickname === context.vars.ownerNickname);
  context.vars.ownerPwd = u.pwd;
  return done();
}

/**
 * Select a word from an auction's description to search
 */
function selectWordToSearch(context, events, done) {
  let a = auctions.sample();
  let words = a.description.split(" ");
  context.vars.searchWord = words.sample();
  return done();
}

/**
 * Select an auction id
 */
function selectAuctionId(context, events, done) {
  let a = auctions.sample();
  context.vars.auctionId = a.id;
  return done();
}

module.exports = {
  uploadImageBody,
  genNewUser,
  genNewAuction,
  genUserUpdate,
  genNewBid,
  genNewReply,
  genNewQuestion,
  processUploadImageReply,
  processNewUserReply,
  processNewAuctionReply,
  processLoginReply,
  processNewBidReply,
  processNewQuestionReply,
  processDeleteUserReply,
  processUpdateUserReply,
  processSearchReply,
  processListAuctionsReply,
  processListQuestionsReply,
  selectImageId,
  selectUserToLogin,
  selectImageToDownload,
  selectAuctionAndUserToBid,
  selectUserToAskQuestion,
  selectOwnerToReply,
  selectUserPwd,
  selectUserSkewed,
  selectWordToSearch,
  selectAuctionId,
};
