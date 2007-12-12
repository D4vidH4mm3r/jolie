/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
 *   Copyright (C) by Claudio Guidi                                        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/

package jolie.runtime;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import jolie.Constants;

class ValueLink extends Value implements Cloneable
{
	private GlobalVariablePath linkPath;
	
	public ValueLink clone()
	{
		return new ValueLink( linkPath );
	}
	
	public void _deepCopy( Value value, boolean copyLinks )
	{
		linkPath.getValue()._deepCopy( value, copyLinks );
	}
	
	public ValueVector getChildren( String childId )
	{
		return linkPath.getValue().getChildren( childId );
	}
	
	public Value getNewChild( String childId )
	{
		return linkPath.getValue().getNewChild( childId );
	}
	
	public Map< String, ValueVector > children()
	{
		return linkPath.getValue().children();
	}
	
	public Map< String, Value > attributes()
	{
		return linkPath.getValue().attributes();
	}
	
	public Value getAttribute( String attributeId )
	{
		return linkPath.getValue().getAttribute( attributeId );
	}
	
	public boolean equals( Value val )
	{
		return linkPath.getValue().equals( val );
	}
	
	public void setStrValue( String value )
	{
		linkPath.getValue().setStrValue( value );
	}
	
	public void setIntValue( int value )
	{
		linkPath.getValue().setIntValue( value );
	}
	
	public void setDoubleValue( double value )
	{
		linkPath.getValue().setDoubleValue( value );
	}
	
	public String strValue()
	{
		return linkPath.getValue().strValue();
	}
	
	public int intValue()
	{
		return linkPath.getValue().intValue();
	}
	
	public double doubleValue()
	{
		return linkPath.getValue().doubleValue();
	}

	public Constants.VariableType type()
	{
		return linkPath.getValue().type();
	}
	
	public ValueLink( GlobalVariablePath path )
	{
		linkPath = path;
	}
	
	public boolean isLink()
	{
		return true;
	}
}

class ValueImpl extends Value
{
	private String strValue = new String();
	private int intValue = 0;
	private double doubleValue = 0;
	private Constants.VariableType type = Constants.VariableType.UNDEFINED;
	
	private ConcurrentHashMap< String, ValueVector > children =
				new ConcurrentHashMap< String, ValueVector >();
	private ConcurrentHashMap< String, Value > attributes =
				new ConcurrentHashMap< String, Value >();
	
	public ValueImpl() {}
	
	public boolean isLink()
	{
		return false;
	}
	
	protected void _deepCopy( Value value, boolean copyLinks )
	{
		assignValue( value );
		Value currVal = null;
		
		// @todo -- are we sure about clearing children and attributes?
		// Probably not.
		/*children.clear();
		attributes.clear();*/

		for( Entry< String, Value > entry : value.attributes().entrySet() ) {
			if ( copyLinks && entry.getValue().isLink() ) {
				currVal = ((ValueLink)entry.getValue()).clone();
			} else {
				currVal = new ValueImpl();
				currVal._deepCopy( entry.getValue(), copyLinks );
			}
			attributes.put( entry.getKey(), currVal );
		}
	
		for( Entry< String, ValueVector > entry : value.children().entrySet() ) {
			ValueVector vec = null;
			if ( copyLinks )
				vec = ValueVector.createClone( entry.getValue() );
			else
				(vec = ValueVector.create()).deepCopy( entry.getValue() );

			children.put( entry.getKey(), vec );
		}
	}
	
	public ValueVector getChildren( String childId )
	{
		ValueVector v = children.get( childId );
		if ( v == null ) {
			v = ValueVector.create();
			children.put( childId, v );
		}
	
		return v;
	}
	
	public Value getNewChild( String childId )
	{
		ValueVector vec = getChildren( childId );
		Value retVal = new ValueImpl();
		vec.add( retVal );
		
		return retVal;
	}
	
	public Map< String, ValueVector > children()
	{
		return children;
	}
	
	public Map< String, Value > attributes()
	{
		return attributes;
	}
	
	public Value getAttribute( String attributeId )
	{
		Value attr = attributes.get( attributeId );
		if ( attr == null ) {
			attr = new ValueImpl();
			attributes.put( attributeId, attr );
		}
		return attr;
	}
	
	public boolean equals( Value val )
	{
		if ( val.isDefined() ) {
			if ( val.isInt() )
				return ( isInt() && intValue() == val.intValue() );
			else if ( val.isDouble() )
				return ( isDouble() && doubleValue() == val.doubleValue() );
			else
				return ( isString() && strValue().equals( val.strValue() ) );
		}
		return( !isDefined() );
	}
	
	public final synchronized void setStrValue( String value )
	{
		type = Constants.VariableType.STRING;
		this.strValue = value;
	}
	
	public final synchronized void setIntValue( int value )
	{
		type = Constants.VariableType.INT;
		this.intValue = value;
	}
	
	public final synchronized void setDoubleValue( double value )
	{
		type = Constants.VariableType.REAL;
		this.doubleValue = value;
	}
	public final synchronized String strValue()
	{
		if ( type == Constants.VariableType.INT )
			return Integer.toString( intValue );
		else if ( type == Constants.VariableType.REAL )
			return Double.toString( doubleValue );
		return strValue;
	}
	
	public final synchronized int intValue()
	{
		if ( type == Constants.VariableType.STRING ) {
			try {
				return Integer.parseInt( strValue );
			} catch( NumberFormatException e ) {
				return strValue.length();
			}
		} else if ( type == Constants.VariableType.REAL )
			return (int)doubleValue;
	
		return intValue;
	}
	
	public final synchronized double doubleValue()
	{
		if ( type == Constants.VariableType.STRING ) {
			try {
				return Double.parseDouble( strValue );
			} catch( NumberFormatException e ) {
				return (double) strValue.length();
			}
		}
		else if ( type == Constants.VariableType.INT ) {
			return (double) intValue;
		}

		return doubleValue;
	}

	public Constants.VariableType type()
	{
		return type;
	}
	
	public ValueImpl( String val )
	{
		super();
		setStrValue( val );
	}
	
	public ValueImpl( int val )
	{
		super();
		setIntValue( val );
	}
	
	public ValueImpl( double val )
	{
		super();
		setDoubleValue( val );
	}
	
	public ValueImpl( Value val )
	{
		if ( val.isDefined() ) {
			if ( val.type() == Constants.VariableType.INT )
				setIntValue( val.intValue() );
			else if ( val.type() == Constants.VariableType.REAL )
				setDoubleValue( val.doubleValue() );
			else
				setStrValue( val.strValue() );
		}
	}
}

/**
 * @author Fabrizio Montesi
 *
 * @todo Make the creation of the necessary internal data lazy? Less performance and less memory consumption.
 */
abstract public class Value implements Expression
{
	public abstract boolean isLink();
	
	public static Value createLink( GlobalVariablePath path )
	{
		return new ValueLink( path );
	}
	
	public static Value create()
	{
		return new ValueImpl();
	}
	
	public static Value create( String str )
	{
		return new ValueImpl( str );
	}
	
	public static Value create( int i )
	{
		return new ValueImpl( i );
	}
	
	public static Value create( double d )
	{
		return new ValueImpl( d );
	}
	
	public static Value create( Value value )
	{
		return new ValueImpl( value );
	}
	
	public static Value createClone( Value value )
	{
		Value retVal = null;
		
		if ( value.isLink() ) {
			retVal = ((ValueLink)value).clone();
		} else {
			retVal = create();
			retVal._deepCopy( value, true );
		}
		
		return retVal;
	}
	
	/**
	 * Makes this value an identical copy of the parameter, considering also its sub-tree.
	 * In case of a sub-link, its pointed Value tree is copied.
	 * @param value The value to be copied. 
	 */
	public synchronized void deepCopy( Value value )
	{
		_deepCopy( value, false );
	}
	
	/*public void deepClone( Value value )
	{
		_deepCopy( value, true );
	}*/
	
	abstract protected void _deepCopy( Value value, boolean copyLinks );
	
	abstract public ValueVector getChildren( String childId );
	
	abstract public Value getNewChild( String childId );
	
	abstract public Map< String, ValueVector > children();

	abstract public Map< String, Value > attributes();
	
	abstract public Value getAttribute( String attributeId );
	
	public Value evaluate()
	{
		return this;
	}
	
	abstract public boolean equals( Value val );
	
	abstract public void setStrValue( String value );
	
	abstract public void setDoubleValue( double value );
	
	public final boolean isInt()
	{
		return ( type() == Constants.VariableType.INT );
	}
	
	public final boolean isDouble()
	{
		return ( type() == Constants.VariableType.REAL );
	}
	
	public final boolean isString()
	{
		return ( type() == Constants.VariableType.STRING );
	}
	
	public final boolean isDefined()
	{
		return ( type() != Constants.VariableType.UNDEFINED );
	}
	
	abstract public void setIntValue( int value );

	abstract public String strValue();
	
	abstract public int intValue();
	
	abstract public double doubleValue();
	
	abstract public Constants.VariableType type();
	
	public final synchronized void add( Value val )
	{
		if ( isDefined() ) {
			if ( isInt() )
				setIntValue( intValue() + val.intValue() );
			else if ( isDouble() )
				setDoubleValue( doubleValue() + val.doubleValue() );
			else
				setStrValue( strValue() + val.strValue() );
		} else
			assignValue( val );
	}
	
	public final synchronized void subtract( Value val )
	{
		if ( !isDefined() )
			assignValue( val );
		else if ( isInt() )
			setIntValue( intValue() - val.intValue() );
		else if ( isDouble() )
			setDoubleValue( doubleValue() - val.doubleValue() );
	}
	
	public final synchronized void multiply( Value val )
	{
		if ( isDefined() ) {
			if ( isInt() )
				setIntValue( intValue() * val.intValue() );
			else if ( isDouble() )
				setDoubleValue( doubleValue() * val.doubleValue() );
		} else
			assignValue( val );
	}
	
	public final synchronized void divide( Value val )
	{
		if ( !isDefined() )
			assignValue( val );
		else if ( isInt() )
			setIntValue( intValue() / val.intValue() );
		else if ( isDouble() )
			setDoubleValue( doubleValue() / val.doubleValue() );
	}
	
	public final synchronized void assignValue( Value val )
	{
		if ( val.isInt() )
			setIntValue( val.intValue() );
		else if ( val.isDouble() )
			setDoubleValue( val.doubleValue() );
		else
			setStrValue( val.strValue() );
	}
}
