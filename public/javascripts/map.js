function init(wsUrl) {
    ymaps.ready(function(){
        var map = new ymaps.Map('myMap', {
            center: [55.76, 37.64],
            zoom: 7,
            behaviors: ["default", "scrollZoom"]
        });
        map.controls.add("mapTools").add("zoomControl").add("typeSelector");
        mapReady(map, wsUrl)
    })
}

function mapReady(map, wsUrl){
    var mapManager = new MapManager(map);

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

function MapManager(map){
    this.map = map;
    this.currentPointCollection = new ymaps.GeoObjectCollection({}, {
            preset: "twirl#greenIcon"
        }
    );
    this.historyPointCollection = new ymaps.GeoObjectCollection({}, {});
    map.geoObjects.add(this.currentPointCollection);
    map.geoObjects.add(this.historyPointCollection);
}

MapManager.prototype = {
    updateCurrentPoint: function(point) {
        var self = this;
        this.currentPointCollection.each(function(placemark){
            self.appendToHistory(placemark);
        });
        this.currentPointCollection.removeAll();
        this.currentPointCollection.add(this.pointToPlacemark(point));
    },

    appendToHistory: function(placemark) {
        this.historyPointCollection.add(placemark);
    },

    pointToPlacemark: function(point) {
        return new ymaps.Placemark([point.latitude, point.longitude],{
            hintContent: point.time
        });
    }
};

