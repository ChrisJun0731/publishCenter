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
        var config = $scope.createConfig("0x03", 1);
        $http.get('/turnOn', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.turnOff = function(){
        var config = $scope.createConfig("0x03", 0);
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

    $scope.getBrightness = function(){
        var config = $scope.createConfig("0x09", null);
        $http.get('/brightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.setBrightness = function(){
        var config = $scope.createConfig("0x11", {brightness: 100});
        $http.get('/setBrightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.autoBrightness = function(){
        var config = $scope.createConfig("0x13", null);
        $http.get('/autoBrightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.screenShot = function(){
        var config = $scope.createConfig("0x17", null);
        $http.get('/screenShot', {params: {config: config}}, function(res){
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
            machines.push(machineObj);

        });
        config.commands = machines;
        config.resources.push($scope.videoName);
        console.log(JSON.stringify(config));
        return JSON.stringify(config);
    };

    function Machine(){
        this.id = new Array();
        this.ip = "";
        this.cmd = "";
        this.data = new Object();
    }

    function Config(){
        this.commands = new Object();
        this.resources = new Array();
    }
});