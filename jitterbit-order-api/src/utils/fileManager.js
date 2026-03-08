const fs = require('fs');
const path = require('path');

function readFile(filePath) {
  return fs.promises.readFile(path.resolve(filePath), 'utf-8');
}

function writeFile(filePath, content) {
  return fs.promises.writeFile(path.resolve(filePath), content, 'utf-8');
}

function exists(filePath) {
  return fs.promises.access(path.resolve(filePath)).then(() => true).catch(() => false);
}

module.exports = { readFile, writeFile, exists };
