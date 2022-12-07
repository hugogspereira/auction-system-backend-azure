const fs = require("fs");
const path = require("path");

function deleteData() {
  try {
    if (fs.existsSync("questions.data")) {
      fs.unlinkSync("questions.data");
    }
    if (fs.existsSync("bids.data")) {
      fs.unlinkSync("bids.data");
    }

    if (fs.existsSync("users.data")) {
      fs.unlinkSync("users.data");
    }

    if (fs.existsSync("auctions.data")) {
      fs.unlinkSync("auctions.data");
    }

    if (fs.existsSync("images.data")) {
      fs.unlinkSync("images.data");
    }
  } catch (e) {
    console.log(e.message);
  }
}

deleteData();
