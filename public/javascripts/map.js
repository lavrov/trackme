function init(wsUrl) {
    ymaps.ready(function(){
        pageReady(
            new ymaps.Map('myMap', {
                center: [55.76, 37.64],
                zoom: 7
            }),
            wsUrl
        )
    })
}

function pageReady(map, wsUrl){
    var mapManager = new MapManager(map);

    ws = new WebSocket(wsUrl);

    ws.onclose = function(){
        alert('Connection lost')
    };

    ws.onmessage = function(event){
        receiveMessage(JSON.parse(event.data))
    };

    function receiveMessage(json) {
        mapManager.updatePosition(json)
    }
}

function MapManager(map){
    this.map = map;
}

MapManager.prototype = {
    updatePosition: function(position) {
        var placemark = new ymaps.Placemark([position.latitude, position.longitude]);
        this.map.geoObjects.add(placemark);
    }
};

