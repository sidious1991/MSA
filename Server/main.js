const dynamo_api = require('./libs/dynamo_api.js');
const sqs_api = require('./libs/sqs_api.js');
const sign = require('./utils/signals.js');
const cp = require('child_process');

/** Creation of queue for publish message, if not exists */
sqs_api.create_queue("notificationQueue");
/** Creation of queue for creation/deletion topic message, if not exists*/
sqs_api.create_queue("creationQueue");
/** Creation of queue for subscription/unsubscription message, if not exists*/
sqs_api.create_queue("subscriptionQueue");
/** Creation of Subscription table */
dynamo_api.creation_table("Subscription");
/** Creation of Topic table */
dynamo_api.creation_table("Topic");

const nr = cp.fork('./readers/notificationReader.js');
const cr = cp.fork('./readers/creationReader.js');
const sr = cp.fork('./readers/subscriptionReader.js');
const sh = cp.fork('./httpServer.js');

const lc = [nr, cr, sr, sh];

sign.handle_signal(lc, 'SIGINT', 'SIGTERM');
