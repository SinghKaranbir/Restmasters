/**
 * Created by karanbir on 30/11/15.
 */
var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var Course = mongoose.model('Course');
var User = mongoose.model('User');
var async = require('async');




router.route('/getCourses/:email?')
    .get(function(req,res){
        if(!req.query.email){
            console.log("param not there");// do something when there is no optionalParam
            Course.find({}, function (err, courses) {
                if(err) console.log(err);

                if(!courses){
                    return res.status(404).send({message: 'No courses Listed'});
                }
                res.status(200).send({courses : courses});
                console.log('courses returned' + [courses]);           
            })
        } else {
            User.find({'email': req.query.email}, function (err, users) {
                if(err) console.log(err);

                if(!users.length){
                    return res.status(404).send({message: 'No users Listed'});
                }
                var objCourses = users[0].courses;
                var arrCourses = [];
                console.log("db couses " + objCourses);
                for (var i=0; i<objCourses.length; i++){
                    console.log("---- id " + objCourses[i]);
                    Course.findById(objCourses[i], function (err, courses) {
                        if(err) console.log(err);

                        if(!courses){
                            return res.status(404).send({message: 'No courses Listed'});
                        }
                        //res.status(200).send({courses : [courses]})
                        arrCourses.push(courses);
                        //console.log("array of courses --> " + arrCourses);           
                    });
                }

            setTimeout(function() {
                console.log('array of courses returned outside --> ' + arrCourses);
                res.status(200).send({courses : arrCourses})
                console.log('courses returned' + users[0].courses);
            }, 3000);
        })
    }

        
        
    });

router.route('/addCourses')
    .put(function(req,res){
        /*var jsonObject = JSON.parse(req.body);
        var email = jsonObject[0];
        console.log("email --> " + email);*/
        console.log("***** " + req.body);
        
        var objCourses = req.body.courses;
        console.log("objCourses" + objCourses);
        var arrCourses = [];

        for (var i=0; i<objCourses.length; i++){
            arrCourses.push(objCourses[i]["_id"]);
        }
        
        console.log('array of courses returned outside --> ' + arrCourses);
        User.find({'email': req.body.email}, function (err, users) {
            if(err) console.log(err);

            console.log("length of users -->" + users.length);
            
            if(users.length == 0){
                    console.log("users not found");
                    return res.status(404).send({message: 'No users Listed'});
            }else{
                User.update({'email': req.body.email}, {$set : {courses: arrCourses}}, {upsert:true}, function(err){
                if (err){
                    console.log(err);
                }else{
                    return res.status(204).send({message: 'Successfully Updated'})
                    }     
                });
            }      
        });
    });


    router.route('/getUsers:courseId?')
        .get(function(req,res){
            console.log("Query -- > " + req.query.courseId);
            User.find({}, function (err, users){
                if(err) console.log(err);
                console.log("length of users " + users.length);
                var courseUsers = [];
                users.forEach(function (user) {
                    var courses = user.courses;
                    for(var i =0; i<courses.length; i++){
                        if(req.query.courseId == courses[i]){
                            courseUsers.push(user);
                        }
                    }
                });
                setTimeout(function() {
                console.log('array of users returned outside --> ' + courseUsers);
                res.status(200).send({users : courseUsers});
                console.log('Users returned' + courseUsers.length);
            }, 3000);
                
            });
        });

module.exports = router;