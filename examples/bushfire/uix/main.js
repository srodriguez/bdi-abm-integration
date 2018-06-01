import 'ol/ol.css';
import GeoJSON from 'ol/format/GeoJSON';
import KML from 'ol/format/KML';
import Map from 'ol/Map';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import sync from 'ol-hashed';
import DragAndDrop from 'ol/interaction/DragAndDrop';
import Draw from 'ol/interaction/Draw';
import Snap from 'ol/interaction/Snap';
import Modify from 'ol/interaction/Modify';
import Select from 'ol/interaction/Select';
import TileLayer from 'ol/layer/Tile';
import XYZSource from 'ol/source/XYZ';
import {fromLonLat} from 'ol/proj';

// const source = new VectorSource({
//   format: new GeoJSON(),
//   url: './data/countries.json'
// });
const source = new VectorSource();

const layer = new VectorLayer({
  source: source
});

const map = new Map({
  target: 'map-container',
  layers: [
    new TileLayer({
      source: new XYZSource({
        url: 'http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg'
      })
    })
  ],
  view: new View({
    center: [0,0],
    zoom: 5
  })
});

map.addLayer(layer);

map.addInteraction(new DragAndDrop({
  source: source,
  formatConstructors: [GeoJSON, KML]
}));

map.addInteraction(new Select({
  source: source
}));


// map.addInteraction(new Draw({
//   type: 'Polygon',
//   source: source
// }));

// map.addInteraction(new Snap({
//   source: source
// }));

// map.addInteraction(new Modify({
//   source: source
// }));


centerMap(map,144.967407,-37.820877); // on Melbourne

sync(map); // for smooth update when moving it around in the browser


function centerMap(theMap, long, lat) {
    console.log("Long: " + long + " Lat: " + lat);
    const coords = fromLonLat([long, lat]);
    theMap.getView().animate({center: coords, zoom: 6});
}
