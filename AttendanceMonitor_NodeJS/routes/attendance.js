/**
 * Created by ananya on 05/06/2016
 */
var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var Course = mongoose.model('Course');
var User = mongoose.model('User');
var Attendance = mongoose.model('Attendance');
var dateFormat = require('dateformat');
//var gcm = require('node-gcm-service');
var gcm = require('node-gcm');
    
    //get attendance
    router.get('/getAttendance/:email?/:course_id?', function (req, res) { 
        console.log("req.params.email" + req.query.email);
        console.log("req.query.course_id" + req.query.course_id);
        //'course_id' : req.query.course_id
        Attendance.find({'email': req.query.email, 'course_id' : req.query.course_id}, function (err, attendances) {
            if(err) console.log(err);

            console.log("attendances.length  " + attendances.length);

            if(!attendances.length){
                return res.status(404).send({message: 'No attendance Listed'});
            }
            setTimeout(function() {
                res.status(200).send({attendance : attendances})
                console.log('attendances returned' + attendances);
            }, 3000);
        });  
    });

    //Mark attendance of User.
    router.post('/mark', function(req, res) {
        
        Attendance.find({ 'email' :  req.body.email, 'course_id': req.body.course_id}, function(err, attendances) {
            console.log("email-----> " + req.body.email);
            // In case of any error, return using the done method
            if (err){
                return res.status(500).send({state: 'failure',message: 'Internal Error'});
            }
            console.log("attendances length --> " + attendances.length);
            var markedAt;
            var markedDay;
            var markedMonth;


            // if attendance marked already
            if (attendances.length >= 1) {
                var now = new Date();
                var currentDay = now.getDay();
                var currentMonth = now.getMonth();
                attendances.forEach(function (attendance) {
                    console.log(attendance.attended_on);
                    markedAt = attendance.attended_on;
                    markedDay = markedAt.getDay();
                    markedMonth = markedAt.getMonth();
                
                });
                if((currentDay == markedDay) && (currentMonth == markedMonth)){
                    sendStudent(req.body.email, "Attendance already marked for " + req.body.email, "Unable to mark attendance");
                    return res.status(422).send({ message: 'Attendance already marked for ' + req.body.email});
                }
                
            }else {

                var attendance = new Attendance();
                attendance.email = req.body.email;
                attendance.course_id = req.body.course_id;
                attendance.save(function (err) {
                    if (err) console.log(err);
                });
                sendStudent(req.body.email, "You have successfully marked attendance", "Attendance Marked");
                sendInstructor(req.body.email + " Marked Attendance", req.body.email + " Marked");
                return res.status(201).send();
                }
        });
    });


    var sendStudent = function(email,msg,title){
        console.log("email in sendStudent ----> " + email);
        User.find({ 'email' :  email }, function (err, user) {
            console.log("sendStudent length ---- >" + user.length);
            console.log("sendStudent user ---- >" + user[0].regId); 
            var studentRegId = user[0].regId;
            console.log("studentRegId--> " + studentRegId);


            var sender = new gcm.Sender('AIzaSyBLddgafq5BLlqXxx_AN0-jgqDNF1f4d-4');
            var message = new gcm.Message({
                priority: 'high',
                timeToLive: 3,
                data: {
                    message: msg,
                    title: title
                }
            });
            var registrationIds = [];
            registrationIds.push(studentRegId);
            sender.send(message, registrationIds, 4, function (err, result) {
                console.log(result);
            }); 
        });
    }

    var sendInstructor = function(msg, title){
        
         User.find({'userType' : 1}, function (err, user){
            if (err){
                return res.status(500).send({state: 'failure',message: 'Internal Error'});
            }
            var instructorRegId = user[0].regId;
            console.log("instructorRegId--> " + instructorRegId);

            var sender = new gcm.Sender('AIzaSyBLddgafq5BLlqXxx_AN0-jgqDNF1f4d-4');
            var message = new gcm.Message({
                priority: 'high',
                timeToLive: 3,
                data: {
                    message: msg,
                    title: title
                }
            });
            var registrationIds = [];
            registrationIds.push(instructorRegId);
            sender.send(message, registrationIds, 4, function (err, result) {
                console.log(result);
            });       
        });
    }

module.exports = router;