function init(wsUrl) {
    var mapReady = $.Deferred(),
        stateReady = $.ajax('/state', {dataType : 'json'});
    ymaps.ready(function(){
        var map = new ymaps.Map('myMap', {
            center: [55.76, 37.64],
            zoom: 7,
            behaviors: ["default", "scrollZoom"]
        });
        preparePreset(map);
        map.controls.add("mapTools").add("zoomControl").add("typeSelector");
        mapReady.resolve(map);
    })
    $.when(mapReady, stateReady.promise()).done(function(map, state){
        pageReady(map, prepareState(state[0]), wsUrl);
    });
}

function prepareState(state) {
    state.currentObject = state.currentObject || undefined;
    return $.extend(state, {
        events: $({}),
        changeObject: function(object) {
            this.currentObject = object;
            this.events.trigger("changeObject", object);
        }
    });
}

function pageReady(map, state, wsUrl){
    var modeSwitcher = new ModeSwitcher(map, state, function(trackingObject){
        return new WebSocket(wsUrl+'?trackingObject='+trackingObject);
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
        initControls(mode, controls, mode.controller);
        controls.attr('data-init', true);
    }
    controls.show();
}

function initControls(mode, $controls, controller) {
    if(mode.name == 'history'){
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
    else if(mode.name == 'online') {
        $controls.find('input[name=trackingObject]').change(function(){
            mode.turnOnTracking($(this).val());
        });
        $.each(mode.trackingDataPresenters, function(object){
            $controls.find('input[value="'+object+'"]').attr('checked', 'checked');
        });
    }
}

function ModeSwitcher(map, state, wsFactory) {
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
            if(!onlineMode) onlineMode = new OnlineMode(map, state, wsFactory);
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

function OnlineMode(map, state, wsFactory) {
    this.map = map;
    this.wsFactory = wsFactory;
    this.state = state;
    this.trackingDataPresenters = {};
    this.events = $({});
}
OnlineMode.prototype = $.extend({}, OperationMode.prototype, {
    name: 'online',
    activate: function(){
        if(this.state.currentObject)
            this.turnOnTracking(this.state.currentObject);
    },
    deactivate: function(){
        $.each(this.trackingDataPresenters, function(i, item){
            item.deactivate();
        });
    },

    turnOnTracking: function(object){
        this.deactivate();
        var presenter = this.trackingDataPresenters[object] || (this.trackingDataPresenters[object] = new TrackingDataPresenter(this, object, this.wsFactory));
        presenter.activate();
        this.state.currentObject = presenter.name;
    }
});

function TrackingDataPresenter(mode, trackingObject, wsFactory) {
    this.mode = mode;
    this.status = 'notActive';
    this.map = mode.map;
    this.webSocketFactory = wsFactory;
    this.name = trackingObject;
    this.trackingObject = trackingObject;
    this.ws = undefined;
    this.currentPointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#greenIcon'
    });
    this.visitedPointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#bluePoint'
    });
}
TrackingDataPresenter.prototype = $.extend({}, OperationMode.prototype, {
    activate: function() {
        if(this.status == 'active') return;
        var self = this;

        this.map.geoObjects.add(this.currentPointCollection);
        this.map.geoObjects.add(this.visitedPointCollection);

        var ws = this.ws = this.webSocketFactory(this.trackingObject);

        ws.onclose = function(){
            alert('Connection lost');
        };
        ws.onmessage = function(event){
            receiveMessage(JSON.parse(event.data));
        };
        function receiveMessage(json) {
            self.updateCurrentPoint(json);
        }
        this.mode.events.trigger('turnOn', this.name);
        this.status = 'active';
        console.log("Tracking activated: "+this.name);
    },
    deactivate: function() {
        if(this.status == 'notActive') return;
        this.map.geoObjects.remove(this.currentPointCollection);
        this.map.geoObjects.remove(this.visitedPointCollection);
        if(this.ws) {
            this.ws.onclose = function(){};
            this.ws.close();
        }
        this.status = 'notActive';
    },

    updateCurrentPoint: function(point) {
        var self = this;
        this.currentPointCollection.each(function(placemark){
            self.visitedPointCollection.add(placemark);
        });
        var placemark = pointToPlacemark(point);
        this.currentPointCollection.add(placemark);
        this.followCurrentPoint();
    },
    followCurrentPoint: function() {
        var self = this;
        this.currentPointCollection.each(function(placemark){
            self.moveToPoint(placemark.geometry, function() {
                self.map.setZoom(15, {smooth: true, position: placemark.geometry});
                var boundsChange = function(){
                    console.log('mode switched');
                    self.map.events.remove('boundschange', boundsChange)
                };
                self.map.events.add('boundschange', boundsChange);
            });
        });
    },
    moveToPoint: function(point, callback) {
        var map = this.map;
        if(!callback) callback = function(){};

        map.panTo(point.getCoordinates(), {
            flying: true,
            callback: callback
        });
    }
});

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

