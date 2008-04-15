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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jolie.runtime.VariablePath;

public class SODEPProtocol extends CommProtocol
{
	public SODEPProtocol( VariablePath configurationPath )
	{
		super( configurationPath );
	}
	
	public SODEPProtocol clone()
	{
		return new SODEPProtocol( configurationPath );
	}

	public void send( OutputStream ostream, CommMessage message )
		throws IOException
	{
		GZIPOutputStream gzip = null;
		String compression = getParameterVector( "compression" ).first().strValue();
		if ( "gzip".equals( compression ) ) {
			gzip = new GZIPOutputStream( ostream );
			ostream = gzip;
		}
		
		DataOutputStream oos = new DataOutputStream( ostream );
		message.writeExternal( oos );
		oos.flush();
		if ( gzip != null )
			gzip.finish();
	}

	public CommMessage recv( InputStream istream )
		throws IOException
	{
		String compression = getParameterVector( "compression" ).first().strValue();
		if ( "gzip".equals( compression ) ) {
			istream = new GZIPInputStream( istream );
		}
		
		DataInputStream ios = new DataInputStream( istream );
		CommMessage ret = null;
		try {
			ret = CommMessage.createFromExternal( ios );
		} catch( ClassNotFoundException e ) {
			throw new IOException( "Received malformed SODEP packet" );
		}
		return ret;
	}
}