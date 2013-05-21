function init(wsUrl) {
    ymaps.ready(function(){
        var map = new ymaps.Map('myMap', {
            center: [55.76, 37.64],
            zoom: 7,
            behaviors: ["default", "scrollZoom"]
        });
        preparePreset(map);
        map.controls.add("mapTools").add("zoomControl").add("typeSelector");
        mapReady(map, wsUrl)
    })
}

function mapReady(map, wsUrl){
    var modeSwitcher = new ModeSwitcher(map, function(){
        return new WebSocket(wsUrl);
    });
    initPanel(map, modeSwitcher);
    modeSwitcher.online();
}

function initPanel(map, modeSwitcher) {
    var $panel = $('#leftPanel');
    var $map = $('#myMap');
    var $toggle = $('#showHideToggle');
    $toggle.on('click', function() {
        if(!this['data-open']) {
            $panel.removeClass('closed');
            $map.addClass('shiftedRight');
            $toggle.addClass('shiftedRight');
        }
        else {
            $panel.addClass('closed');
            $map.removeClass('shiftedRight');
            $toggle.removeClass('shiftedRight');
        }
        map.container.fitToViewport();
        this['data-open'] = !this['data-open'];
    });
    var $modeSwitcher = $panel.find('.modeSwitcher');
    $modeSwitcher.on('click', 'button', function(event) {
        modeSwitcher[event.target.name]();
    });
    modeSwitcher.events.on('modeSwitch', function(event, mode){
        switchPanel($modeSwitcher, mode);
    });
    return $toggle;
}

function switchPanel($modeSwitcher, mode) {
    $modeSwitcher.find('button').removeClass('active');
    $modeSwitcher.find('button[name='+mode.name+']').addClass('active');
    $('.modeControls').hide();
    var controls = $('#'+mode.name);
    var isInitComplete = controls.attr('data-init');
    if(!isInitComplete) {
        initControls(mode.name, controls, mode.controller);
        controls.attr('data-init', true);
    }
    controls.show();
}

function initControls(modeName, $controls, controller) {
    if(modeName == 'history'){
        var $form = $controls.find('form');
        var $customTime = $form.find('.customTime');
        $form.find('input[name=intervalType]').change(function(){
            if($(this).val() == 'custom')
                $customTime.show();
            else
                $customTime.hide();
        })
        $('#showHistory').on('click', function(){
            $.ajax({
                type : 'POST',
                url : $form.attr('action'),
                data : $form.serialize(),
                dataType : "text",
                success : function(data) {
                    console.log(data);
                    var points = eval(data);
                    controller.displayPoints(points);
                }
            });
        });
    }
    else if(modeName == 'online') {
        $controls.find('#followCurrentPoint').click(function(){
            controller.followCurrentPoint();
        });
    }
}

function ModeSwitcher(map, wsFactory) {
    var mode = new OperationMode();
    var events = $({});
    function deactivatePrevMode() {
        mode.deactivate();
        mode = undefined;
    }
    function activate(newMode) {
        mode = newMode;
        mode.activate();
        events.trigger('modeSwitch', mode);
        return mode;
    }
    var onlineMode = undefined;
    var historyMode = undefined;
    return {
        events: events,
        online: function() {
            deactivatePrevMode();
            if(!onlineMode) onlineMode = new OnlineMode(map, wsFactory);
            return activate(onlineMode);
        },
        history: function() {
            deactivatePrevMode();
            if(!historyMode) historyMode = new HistoryMode(map);
            return activate(historyMode);
        }
    }
}

function OperationMode(){}
OperationMode.prototype = {
    name: "Dummy mode",
    activate: function(){},
    deactivate: function(){},
    controller: undefined
}

function OnlineMode(map, wsFactory) {
    this.map = map;
    this.webSocketFactory = wsFactory;
    this.ws = undefined;
    this.currentPointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#greenIcon'
    });
    this.visitedPointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#bluePoint'
    });
    this.controller = new OnlineModeController(this)
}
OnlineMode.prototype = $.extend({}, OperationMode.prototype, {
    name: 'online',
    activate: function() {
        var self = this;

        this.map.geoObjects.add(this.currentPointCollection);
        this.map.geoObjects.add(this.visitedPointCollection);

        var ws = this.ws = this.webSocketFactory();

        ws.onclose = function(){
            alert('Connection lost');
        };
        ws.onmessage = function(event){
            receiveMessage(JSON.parse(event.data));
        };
        function receiveMessage(json) {
            self.controller.updateCurrentPoint(json);
        }
    },
    deactivate: function() {
        this.map.geoObjects.remove(this.currentPointCollection);
        this.map.geoObjects.remove(this.visitedPointCollection);
        if(this.ws) {
            this.ws.onclose = function(){};
            this.ws.close();
        }
    }
});

function OnlineModeController(mode) {
    return {
        updateCurrentPoint: function(point) {
            mode.currentPointCollection.each(function(placemark){
                mode.visitedPointCollection.add(placemark);
            });
            var placemark = pointToPlacemark(point);
            mode.currentPointCollection.add(placemark);
            this.followCurrentPoint();
        },
        followCurrentPoint: function() {
            var self = this;
            mode.currentPointCollection.each(function(placemark){
                self.moveToPoint(placemark.geometry, function() {
                    mode.map.setZoom(15, {smooth: true, position: placemark.geometry});
                    var boundsChange = function(){
                        console.log('mode switched');
                        mode.map.events.remove('boundschange', boundsChange)
                    };
                    mode.map.events.add('boundschange', boundsChange);
                });
            });
        },
        moveToPoint: function(point, callback) {
            var map = mode.map;
            if(!callback) callback = function(){};

            map.panTo(point.getCoordinates(), {
                flying: true,
                callback: callback
            });
        }
    }
}

function HistoryMode(map) {
    this.map = map;
    this.pointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#bluePoint'
    });
    this.controller = HistoryModeController(this);
}
HistoryMode.prototype = $.extend({}, OperationMode.prototype, {
    name: 'history',
    activate: function() {
        this.map.geoObjects.add(this.pointCollection);
    },
    deactivate: function() {
        this.map.geoObjects.remove(this.pointCollection);
    }
});

function HistoryModeController(mode) {
    return {
        displayPoints: function(points) {
            this.clearHistory();
            $.each(points, function(i, point){
                mode.pointCollection.add(pointToPlacemark(point));
            });
            mode.map.setBounds(mode.pointCollection.getBounds());
        },

        clearHistory: function() {
            mode.pointCollection.removeAll();
        }
    };
}

function pointToPlacemark(point) {
    var date = new Date(point.time);
    var formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    return new ymaps.Placemark([point.latitude, point.longitude],{
        hintContent: formattedDate
    });
}

function preparePreset(map) {
    var template = ymaps.option.presetStorage.get('twirl#blueIcon');
    var preset =  $.extend({}, template, {
        iconImageHref: '/assets/images/map/blue_point.png',
        iconImageOffset: [-6, -6],
        iconImageSize: [13, 13]
    });
    ymaps.option.presetStorage.add('twirl#bluePoint', preset);
}

