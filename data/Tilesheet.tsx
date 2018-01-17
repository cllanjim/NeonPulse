<?xml version="1.0" encoding="UTF-8"?>
<tileset name="Tilesheet" tilewidth="64" tileheight="64" tilecount="3" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0">
  <image width="64" height="64" source="tiles/floor_tile.jpg"/>
 </tile>
 <tile id="1">
  <image width="64" height="64" source="tiles/pit.jpg"/>
  <objectgroup draworder="index" name="pit">
   <object id="1" x="0" y="-0.25" width="63.75" height="64.75"/>
  </objectgroup>
 </tile>
 <tile id="3">
  <image width="64" height="64" source="tiles/wall_tile2.jpg"/>
  <objectgroup draworder="index" name="wall">
   <object id="1" x="-0.25" y="-0.25" width="64" height="64.25"/>
  </objectgroup>
 </tile>
</tileset>
