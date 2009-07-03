/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
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

package jolie.net;

import java.io.IOException;
import jolie.Interpreter;

/**
 * TODO: this shouldn't be polled.
 */
public class LocalCommChannel extends ListCommChannel implements PollableCommChannel
{
	final private Interpreter interpreter;
	final private CommListener listener;
	
	public LocalCommChannel( Interpreter interpreter, CommListener listener )
	{
		this.interpreter = interpreter;
		this.listener = listener;
	}

	public Interpreter interpreter()
	{
		return interpreter;
	}
	
	@Override
	protected void sendImpl( CommMessage message )
	{
		synchronized( olist ) {
			olist.add( message );
		}
		assert( interpreter != null );
		assert( interpreter.commCore() != null );
		interpreter.commCore().scheduleReceive(
					new ListCommChannel( olist, ilist ), listener
				);
	}
	
	public boolean isReady()
	{
		return( !ilist.isEmpty() );
	}
	
	@Override
	protected void disposeForInputImpl()
		throws IOException
	{
		Interpreter.getInstance().commCore().registerForPolling( this );
	}
}
