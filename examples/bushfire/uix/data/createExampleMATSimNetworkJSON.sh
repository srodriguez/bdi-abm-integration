dir=`dirname "$0"`

matsim2esri=$dir/../../scripts/matsimNetwork2ESRI.sh
esri2json=$dir/../../scripts/shapefile2json.py

function toJson() {
  prefix=$1
  crs=$2
  cmd="rm -f $dir/${prefix}/${prefix}L.json; $esri2json -infile $dir/${prefix}/${prefix}L.shp -outfile $dir/${prefix}/${prefix}L.json -inCRS $crs -outCRS 'EPSG:4326'"
  echo $cmd; eval $cmd
  cmd="rm -f $dir/${prefix}/${prefix}P.json; $esri2json -infile $dir/${prefix}/${prefix}P.shp -outfile $dir/${prefix}/${prefix}P.json -inCRS $crs -outCRS 'EPSG:4326'"
  echo $cmd; eval $cmd

}
# Mount Alexander Shire network JSON
name=mount_alexander_shire_network
projection="EPSG:28355"
mkdir -p $dir/$name
cmd="$matsim2esri -net $dir/../../scenarios/mount-alexander-shire/mount_alexander_shire_network.xml.gz -outl $dir/$name/${name}L.shp -outp $dir/$name/${name}P.shp -crs 'EPSG:4326'"
echo $cmd; eval $cmd
toJson $name $projection

# Surf Coast Shire network JSON
name=surf_coast_shire_network
projection="EPSG:32754"
mkdir -p $dir/$name
cmd="$matsim2esri -net $dir/../../scenarios/surf-coast-shire/surf_coast_shire_network.xml.gz -outl $dir/$name/${name}L.shp -outp $dir/$name/${name}P.shp -crs 'EPSG:4326'"
echo $cmd; eval $cmd
toJson $name $projection
