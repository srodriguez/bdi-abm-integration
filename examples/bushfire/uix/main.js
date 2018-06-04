import 'ol/ol.css';
/*
import Projection from 'ol/proj/projection';
import View from 'ol/view';
import Map from 'ol/map';
import Tile from 'ol/layer/tile';
import OSM from 'ol/source/osm';
import GeoJSON from 'ol/format/geojson';
import Vector from 'ol/layer/vector';
import VectorTile from 'ol/source/vectortile';
*/
import geojsonvt from 'geojson-vt/geojson-vt-dev';

import DragAndDrop from 'ol/interaction/draganddrop';
import Overlay from 'ol/overlay';
import {Style, Fill, Stroke, Circle, Text} from 'ol/style';
import Select from 'ol/interaction/select';

var replacer = function(key, value) {
  if (value.geometry) {
    var type;
    var rawType = value.type;
    var geometry = value.geometry;

    if (rawType === 1) {
      type = 'MultiPoint';
      if (geometry.length == 1) {
        type = 'Point';
        geometry = geometry[0];
      }
    } else if (rawType === 2) {
      type = 'MultiLineString';
      if (geometry.length == 1) {
        type = 'LineString';
        geometry = geometry[0];
      }
    } else if (rawType === 3) {
      type = 'Polygon';
      if (geometry.length > 1) {
        type = 'MultiPolygon';
        geometry = [geometry];
      }
    }

    return {
      'type': 'Feature',
      'geometry': {
        'type': type,
        'coordinates': geometry
      },
      'properties': value.tags
    };
  } else {
    return value;
  }
};

var tilePixels = new ol.proj.Projection({
  code: 'TILE_PIXELS',
  units: 'tile-pixels'
});

var map = new ol.Map({
  layers: [
    new ol.layer.Tile({
      source: new ol.source.OSM()
    })
  ],
  target: 'map',
  view: new ol.View({
    center: [0, 0],
    zoom: 2
  })
});

var url = 'data/surf_coast_shire_network/surf_coast_shire_networkP.json';
fetch(url).then(function(response) {
  return response.json();
}).then(function(json) {
  var tileIndex = geojsonvt(json, {
    extent: 4096,
    debug: 2
  });
  var vectorSource = new ol.source.VectorTile({
    format: new ol.format.GeoJSON(),
    tileLoadFunction: function(tile) {
      var format = tile.getFormat();
      var tileCoord = tile.getTileCoord();
      var data = tileIndex.getTile(tileCoord[0], tileCoord[1], -tileCoord[2] - 1);

      var features = format.readFeatures(
          JSON.stringify({
            type: 'FeatureCollection',
            features: data ? data.features : []
          }, replacer));
      tile.setLoader(function() {
        tile.setFeatures(features);
        tile.setProjection(tilePixels);
      });
    },
    url: 'data:' // arbitrary url, we don't use it in the tileLoadFunction
  });
  var vectorLayer = new ol.layer.VectorTile({
    source: vectorSource,
    style: function(feature, resolution){
      var w = feature.getProperties()["capacity"]/900;
      var styles = {
        'Polygon': [new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'rgba(102, 153, 255, 0.7)',
                width: w
            })
        })],
        'LineString': [new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'rgba(102, 153, 255, 0.7)',
                width: w
            })
        })]
      };
        return styles[feature.getGeometry().getType()];
    }
  });
  map.addLayer(vectorLayer);

  // const overlay = new Overlay({
  //   element: document.getElementById('popup-container'),
  //   positioning: 'bottom-center',
  //   offset: [0, -10],
  //   autoPan: true
  // });
  //map.addOverlay(overlay); <-- gives an error
  // overlay.getElement().addEventListener('click', function() {
  //   overlay.setPosition();
  // });

  map.on('click', function(e) {
    let markup = '';
    map.forEachFeatureAtPixel(e.pixel, function(feature) {
      markup += `${markup && '<hr>'}<table>`;
      const properties = feature.getProperties();
      for (const property in properties) {
        markup += `<tr><th>${property}</th><td>${properties[property]}</td></tr>`;
      }
      markup += '</table>';
    }, {hitTolerance: 1});
    if (markup) {
      document.getElementById('popup-content').innerHTML = markup;
      //overlay.setPosition(e.coordinate);
      console.log(markup)
    } else {
      //overlay.setPosition();
    }
  });

  var select = new ol.interaction.Select({
      layers: [vectorLayer],
      style: function(feature, resolution){
        var w = feature.getProperties()["capacity"]/900;
        var styles = {
          'Polygon': [new ol.style.Style({
              stroke: new ol.style.Stroke({
                  color: 'rgba(255, 102, 0, 0.7)',
                  width: w
              })
          })],
          'LineString': [new ol.style.Style({
              stroke: new ol.style.Stroke({
                  color: 'rgba(255, 102, 0, 0.7)',
                  width: w
              })
          })]
        };
        return styles[feature.getGeometry().getType()];
      }
    });
    map.addInteraction(select);
});
