/*
 * AVRS - http://avrs.sourceforge.net/
 *
 * Copyright (C) 2011 John Gorkos, AB0OO
 *
 * AVRS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * AVRS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AVRS; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package net.ab0oo.aprs.parser;

import java.util.Date;
import java.util.Locale;
/**
 * 
 * @author johng
 * This class represents a Position as specified by the APRS specification.  This includes
 * a symbol table and actual symbol, and a possible timestamp.
 *
 * A negative positionAmbiguity indicates high-precision position with DAO extension
 * -1: single digit (plaintext) DAO
 * -2: radix91 DAO
 */
public class Position implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private double latitude = 0d, longitude = 0d;
	private int altitude = -1;
	private int positionAmbiguity;
	private Date timestamp;
	private char symbolTable, symbolCode;
	private String csTField = " sT";

	public Position() {
	    timestamp = new Date();
	}
	
	public Position(double lat, double lon, int posAmb, char st, char sc) {
		// no more rounding here
		this.latitude = lat;
		this.longitude = lon;
		this.positionAmbiguity = posAmb;
		this.symbolTable = st;
		this.symbolCode = sc;
		this.timestamp = new Date();
	}
	
	public Position(double lat, double lon) {
		// no more rounding here
		this.latitude = lat;
		this.longitude = lon;
		//this.latitude = Math.round(lat * 100000) * 0.00001D;
		//this.longitude = Math.round(lon * 100000) * 0.00001D;
		this.positionAmbiguity=0;
		this.symbolTable = '\\';
		this.symbolCode = '.';
		this.timestamp = new Date();
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		if(this.positionAmbiguity>=0) return Math.round(latitude * 100000) * 0.00001D;
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		if(this.positionAmbiguity>=0) return Math.round(longitude * 100000) * 0.00001D;
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public int getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the positionAmbiguity
	 */
	public int getPositionAmbiguity() {
		return positionAmbiguity;
	}

	/**
	 * @param positionAmbiguity the positionAmbiguity to set
	 */
	public void setPositionAmbiguity(int positionAmbiguity) {
		this.positionAmbiguity = positionAmbiguity;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the symbolTable
	 */
	public char getSymbolTable() {
		return symbolTable;
	}

	/**
	 * @param symbolTable the symbolTable to set
	 */
	public void setSymbolTable(char symbolTable) {
		this.symbolTable = symbolTable;
	}

	/**
	 * @return the symbolCode
	 */
	public char getSymbolCode() {
		return symbolCode;
	}

	/**
	 * @param symbolCode the symbolCode to set
	 */
	public void setSymbolCode(char symbolCode) {
		this.symbolCode = symbolCode;
	}

	private static int dao91(double x) {
		/* radix91(xx/1.1) of dddmm.mmxx */
		/* we must make sure that rounding is exactly the same as in getDMS */
		int minFrac = (int)Math.round(x*600000);
		boolean negative = (minFrac < 0);
		if(negative) minFrac = -minFrac;
		//int deg = minFrac / 600000;
		//int min = (minFrac/10000) % 60;
		minFrac = minFrac % 10000;

		int res = ( (minFrac%100)*20+11)/22;	
		System.out.println("dao91 of "+x+" => minfrac "+minFrac+" base91="+res);
		return res;
	}
	private static int daosingle(double x) {
		/* we must make sure that rounding is exactly the same as in getDMS */
		int minFrac = (int)Math.round(x*600000);
		boolean negative = (minFrac < 0);
		if(negative) minFrac = -minFrac;
		//int deg = minFrac / 600000;
		//int min = (minFrac/10000) % 60;
		minFrac = minFrac % 10000;

		int res = (minFrac%100)/10;
		System.out.println("daosingle of "+x+" => minfrac "+minFrac+" digit="+res);
		return res;
	}

	public String getDAO() {
		if(positionAmbiguity>=0) {
			return "";
		}
		if(positionAmbiguity==-1) {
			return "!W"+daosingle(latitude)+daosingle(longitude)+"!";
		}
		return "!w"+(char)(dao91(latitude)+33)+(char)(dao91(longitude)+33)+"!";
	}
	
	public String getDMS(double decimalDegree, boolean isLatitude) {
			int minFrac = (int)Math.round(decimalDegree*600000); ///< degree in 1/100s of a minute
			boolean negative = (minFrac < 0);
			if (negative)
					minFrac = -minFrac;
			int deg = minFrac / 600000;
			int min = (minFrac / 10000) % 60;
			minFrac = positionAmbiguity >= 0 ? (int)Math.round((minFrac % 10000)*0.01) : (minFrac%10000)/100;
			String ambiguousFrac;

			switch (positionAmbiguity) {
			case 1: // "dd  .  N"
				ambiguousFrac = "  .  "; break;
			case 2: // "ddm .  N"
				ambiguousFrac = String.format((Locale)null, "%d .  ", min/10); break;
			case 3: // "ddmm.  N"
				ambiguousFrac = String.format((Locale)null, "%02d.  ", min); break;
			case 4: // "ddmm.f N"
				ambiguousFrac = String.format((Locale)null, "%02d.%d ", min, minFrac/10); break;
			default: // "ddmm.ffN"
				ambiguousFrac = String.format((Locale)null, "%02d.%02d", min, minFrac); break;
			}
			if ( isLatitude ) {
				return String.format((Locale)null, "%02d%s%s", deg, ambiguousFrac, ( negative ? "S" : "N"));
			} else {
				return String.format((Locale)null, "%03d%s%s", deg, ambiguousFrac, ( negative ? "W" : "E"));
			}
	}
	
	@Override
	public String toString() {
		return getDMS(latitude,true)+symbolTable+getDMS(longitude,false)+symbolCode;
	}
	
	public String toDecimalString() {
		return getLatitude()+", "+getLongitude();
	}

	public void setCsTField(String val) {
		if(val == null || val == "") {
			val = " sT";
		}
		csTField = val;
	}

	public String getCsTField() {
		return csTField;
	}

	public String toCompressedString() {
		long latbase = Math.round(380926 * (90-this.latitude));
		long latchar1 = latbase / (91*91*91)+33;
		latbase = latbase % (91*91*91);
		long latchar2 = latbase / (91*91)+33;
		latbase = latbase % (91*91);
		int latchar3 = (int)(latbase / 91)+33;
		int latchar4 = (int)(latbase % 91)+33;
		long lonbase = Math.round(190463 * (180+this.longitude));
		long lonchar1 = lonbase / (91*91*91)+33;
		lonbase %= (91*91*91);
		long lonchar2 = lonbase / (91*91)+33;
		lonbase = lonbase % (91*91);
		int lonchar3 = (int)(lonbase / 91)+33;
		int lonchar4 = (int)(lonbase % 91)+33;
		
		return ""+symbolTable+(char)latchar1+(char)latchar2+(char)latchar3+(char)latchar4+
				""+(char)lonchar1+(char)lonchar2+(char)lonchar3+(char)lonchar4+symbolCode+csTField;
	}

	public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
		    double earthRadius = 3958.75;
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLng = Math.toRadians(lng2-lng1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double dist = earthRadius * c;

		    return new Float(dist).floatValue();
    }
	
	public float distance(Position position2) {
		double lat1 = this.getLatitude();
		double lat2 = position2.getLatitude();
		double lng1 = this.getLongitude();
		double lng2 = position2.getLongitude();
		return distFrom(lat1,lng1,lat2,lng2);
	}
	
	/**
	 * Returns Azimuth between two points as measured clockwise from North
	 * and vary from 0° to 360°.
	 * <p>
	 * This function returns the initial bearing (sometimes referred to as forward azimuth)
	 * which if followed in a straight line along a great-circle arc will take you from the
	 * start point to the end point.
	 * </p>
	 * @param position2 end position
	 * @return bearing in degrees
	 */
	public float direction(Position position2) {
		double Lat1 = Math.toRadians(position2.getLatitude());
		double Lon1 = position2.getLongitude();
		double Lat2 = Math.toRadians(getLatitude());
		double Lon2 = getLongitude();
		double dLon = Math.toRadians(Lon2 - Lon1);
		double y = Math.sin(dLon) * Math.cos(Lat2);
		double x = Math.cos(Lat1) * Math.sin(Lat2) - Math.sin(Lat1) *
			Math.cos(Lat2) * Math.cos(dLon);
		return (float) ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360);
	}

	public static void main(String[] args) {
		Position pos = new Position();
		pos.setPositionAmbiguity(-1);
		pos.setLatitude(34.1255911);
		pos.setLongitude(-84.13697);
		pos.setSymbolCode('o');
		pos.setSymbolTable('/');
		System.out.println("latitude is "+pos.getLatitude());
		System.out.println("Position string is "+pos.toString());
		System.out.println("Position DAO string is "+pos.getDAO());
		System.out.println("Compressed string is "+pos.toCompressedString());

		System.out.println("dao(15) is "+dao91(15));
		System.out.println("dao(15°00.04) is "+dao91(15+4.0/6000));
		System.out.println("dao(15°00.043) is "+dao91(15+4.3/6000));
		System.out.println("dao(-15°00.043) is "+dao91(-15-4.3/6000));
		System.out.println("dao(15°00.0499) is "+dao91(-15-4.99/6000));
	}

}
