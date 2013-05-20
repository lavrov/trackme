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
    var mapManager = new MapManager(map);

    initPanel(map, mapManager);

    ws = new WebSocket(wsUrl);

    ws.onclose = function(){
        alert('Connection lost')
    };

    ws.onmessage = function(event){
        receiveMessage(JSON.parse(event.data))
    };

    function receiveMessage(json) {
        mapManager.updateCurrentPoint(json)
    }
}

function initPanel(map, mapManager) {
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
        panelModeChange(event.target.name, mapManager);
        $modeSwitcher.find('button').removeClass('active');
        $(event.target).addClass('active');
    })
}

function panelModeChange(mode, mapManager) {
    $('.modeControls').hide();
    var controls = $('#'+mode);
    var isInitComplete = controls.attr('data-init');
    if(!isInitComplete) {
        initControls(mode, mapManager, controls);
        controls.attr('data-init', true);
    }
    controls.show();
}

function initControls(mode, mapManager, $controls) {
    if(mode == 'history'){
        var $form = $controls.find('form');
        $('#showHistory').on('click', function(){
            $.ajax({
                type : 'POST',
                url : $form.attr('action'),
                data : $form.serialize(),
                dataType : "text",
                success : function(data) {
                    console.log(data);
                    var points = eval(data);
                    $.each(points, function(i, point){
                        mapManager.appendToHistory(point);
                    });
                }
            });
        });
    }
}

function MapManager(map){
    this.map = map;
    this.currentPointCollection = new ymaps.GeoObjectCollection({}, {
            preset: "twirl#greenIcon"
        }
    );
    this.historyPointCollection = new ymaps.GeoObjectCollection({}, {
        preset: 'twirl#bluePoint'
    });
    map.geoObjects.add(this.currentPointCollection);
    map.geoObjects.add(this.historyPointCollection);
}

MapManager.prototype = {
    updateCurrentPoint: function(point) {
        var self = this;
        this.currentPointCollection.each(function(placemark){
            self.historyPointCollection.add(placemark);
        });
        this.currentPointCollection.add(this.pointToPlacemark(point));
    },

    appendToHistory: function(point) {
        this.historyPointCollection.add(this.pointToPlacemark(point));
    },

    clearHistory: function() {
        this.historyPointCollection.removeAll();
    },

    pointToPlacemark: function(point) {
        var date = new Date(point.time);
        var formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
        return new ymaps.Placemark([point.latitude, point.longitude],{
            hintContent: formattedDate
        });
    }
};

function preparePreset(map) {
    var template = ymaps.option.presetStorage.get('twirl#blueIcon');
    var preset =  $.extend({}, template, {
        iconImageHref: '/assets/images/map/blue_point.png',
        iconImageSize: [13, 13]
    });
    ymaps.option.presetStorage.add('twirl#bluePoint', preset);
}

