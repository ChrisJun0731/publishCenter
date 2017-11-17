var app = angular.module('app', []);
app.controller('appCtrl', function($scope, $http){

    $scope.getConfig = function(){
        $http.get('/serverConfig', function(res){
            return res.data;
        }).then(function(rs){
            $scope.config = rs.data;
        });
    };

    $scope.turnOn = function(){
        var config = $scope.createConfig("0x03", {switch: 1});
        //构造无效的json测试1(可以被JSONUtils检测出来)
//        config = config.substr(1, config.lenght-1);
        //构造无效json测试2(不能被JSONUtils检测出来)
//        config = "{commands:[], array}";
        //构造值为空的json测试
//        config = "{commands:[], array:[]}";
        $http.get('/turnOn', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.turnOff = function(){
        var config = $scope.createConfig("0x03", {switch: 0});
        $http.get('/turnOff', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.getTime = function(){
        var config = $scope.createConfig("0x07", null);
        $http.get('/time', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.getStatus = function(){
        var config = $scope.createConfig("0x15", null);
        $http.get('/status', {params: {config: config}}, function(res){
            return res.data;
            }).then(function(rs){
                return rs.data;
        });
    }

    $scope.getBrightness = function(){
        var config = $scope.createConfig("0x09", null);
        $http.get('/brightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.setBrightness = function(){
        var config = $scope.createConfig("0x11", {brightness: 255});
        $http.get('/setBrightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

//    $scope.autoBrightness = function(){
//        var config = $scope.createConfig("0x13", null);
//        $http.get('/autoBrightness', {params: {config: config}}, function(res){
//            return res.data;
//        }).then(function(rs){
//            return rs.data;
//        });
//    };

    $scope.getScreenParameter = function(){
        var config = $scope.createConfig("0x01");
        $http.get('/screenParameter', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    }

    $scope.screenShot = function(){
        var config = $scope.createConfig("0x17", null);
        $http.get('/screenShot', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    }

    $scope.convertMessage = function(){
        var message = $scope.sendMessage;
        $http.get('/convertMessage', {params: {message: message}}, function(res){
            return res.data;
        }).then(function(rs){
            console.log(rs);
            console.log(rs.data.frame);
            $scope.frame = rs.data.frame;
        });
    }

    $scope.sendPlayList = function(){
        var data = [
                        {
                            "program":{
                                "stayTime": 100,
                                "units": [
                                    {
                                        "video": {
                                            "x": 0,
                                            "y": 0,
                                            "h": 192,
                                            "w": 320,
                                            "filename": "666.mp4"
                                        }
                                    }
                                ]
                            }
                        }
                    ];
        var resources = [{"filename": "666.mp4", "filepath": "/resources/666.mp4", "filetype": 1}];
//        var resources = [];
        var config = $scope.createConfig("0x31", data, resources);
        $http.get('/sendPlayList', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    }

    $scope.createConfig = function(cmd, data){
        var machines = new Array();
        var config = new Config();
        angular.forEach($scope.config.machines, function(machine){
            var machineObj = new Machine();
            machineObj.ip = machine.machine.ip;
            machineObj.cmd = cmd;
            machineObj.data = data;
            angular.forEach(machine.machine.screens, function(screen){
                if(screen.selected == true){
                    machineObj.id.push(screen.id);
                }
            });
            if(machineObj.id.length != 0){
                machines.push(machineObj);
            }
        });
        config.commands = machines;
        config.resources = [];
        config.ip = $scope.config.ip;
        config.port = $scope.config.port;
        console.log(JSON.stringify(config));
        return JSON.stringify(config);
    };

    $scope.createConfig = function(cmd, data, resources){
            var machines = new Array();
            var config = new Config();
            angular.forEach($scope.config.machines, function(machine){
                var machineObj = new Machine();
                machineObj.ip = machine.machine.ip;
                machineObj.cmd = cmd;
                machineObj.data = data;
                angular.forEach(machine.machine.screens, function(screen){
                    if(screen.selected == true){
                        machineObj.ids.push(screen.id);
                    }
                });
                if(machineObj.ids.length != 0){
                    machines.push(machineObj);
                }
            });
            config.commands = machines;
            config.resources = resources;
            config.ip = $scope.config.ip;
            config.port = $scope.config.port;
            console.log("客户端生成json: " + JSON.stringify(config));
            return JSON.stringify(config);
        };

    function Machine(){
        this.ids = new Array();
        this.ip = "";
        this.cmd = "";
        this.data = new Object();
    }

    function Config(){
        this.ip = "";
        this.port = 5000;
        this.commands = new Object();
        this.resources = new Array();
    }
});