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

package jolie.process;

import java.util.Vector;

import jolie.ExecutionThread;
import jolie.runtime.FaultException;

public class SequentialProcess implements Process
{
	final private Vector< Process > children;
	
	public SequentialProcess()
	{
		children = new Vector< Process >();
	}
	
	public Process clone( TransformationReason reason )
	{
		SequentialProcess p = new SequentialProcess();
		for( Process child : children )
			p.addChild( child.clone( reason ) );
		return p;
	}
	
	public void run()
		throws FaultException
	{
		final ExecutionThread ethread = ExecutionThread.currentThread();
		for( Process proc : children ) {
			if ( ethread.isKilled() && proc.isKillable() ){
				return;
			}
			proc.run();
		}
	}
	
	public void addChild( Process process )
	{
		if ( process != null )
			children.add( process );
	}
	
	public boolean isKillable()
	{
		return children.get( 0 ).isKillable();
	}
}
