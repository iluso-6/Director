# Director

# An application solution to mute mobile devices within a given geographical area
This involves the use of <em>two</em> applications - a master and a client.

•	The client application should install and close while continuing a background location Service.

•	The client Service should also be available trigger the speaker mute.<br> 
•	The controller application should list the clients, their current status and show them on a map.<br> 
•	The controller application should set the geo fence area on the map and update Firebase.<br> 

Main Application requirements<br> 
•	Both applications will require a common Firebase database<br> 
•	The database should be secure with read/write authorised rules<br> 
•	Both applications will require Firebase authentication login as a result<br> 


<table>
  <tr>
    <td><img src="https://github.com/iluso-6/Director/blob/master/screenshots/auth.png?raw=true" align="left"/></td>
    <td width="33%"></td>
    <td> <img src="https://github.com/iluso-6/Director/blob/master/screenshots/client_permission_dialog.png?raw=true" align="right"/>
    </td>

<br><br>

  </tr>
  
</table>

<table>
  <tr>
    <td><img src="https://github.com/iluso-6/Director/blob/master/screenshots/master.png?raw=true" align="left"/></td>
    <td width="33%"></td>
    <td> <img src="https://github.com/iluso-6/Director/blob/master/screenshots/master_map.png?raw=true" align="right"/>
    </td>

<br><br>

  </tr>
  
</table>

<table>
  <tr>
        <td width="33%"></td>
    <td><img src="https://github.com/iluso-6/Director/blob/master/screenshots/master_map_night.png?raw=true" align="center"/></td>
   

<br><br>

  </tr>
  
</table>
