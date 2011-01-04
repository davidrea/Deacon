/*
 * COPYRIGHT (c) 2010 The Members of the Deacon Project <http://deacon.daverea.com/core-team/>
 * 
 * This file is part of Deacon
 * 
 * Deacon is free software: you can redistribute it and/or modify it under the terms of the GNU 
 * Lesser General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * 
 * Deacon is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License and the GNU Lesser General 
 * Public License along with Deacon.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.deacon;

/**
 * Implements an error class to describe different errors that
 * the DeaconService could throw. Used by clients to determine
 * why events may have happend.
 */
public class DeaconError {

	private Exception error;
	private DeaconErrorType errorType;
	
	/**
	 * Default constructor that sets the original exception and
	 * defaults the exception type
	 * @param e is the original exception that was thrown
	 */
	public DeaconError(Exception e){
		error = e;
		errorType = DeaconErrorType.UnknownError;
	}
	
	/**
	 * Constructor that sets both the original error and the 
	 * error type
	 * @param e is the original error thrown
	 * @param type is the specific error type
	 */
	public DeaconError(Exception e, DeaconErrorType type){
		error = e;
		errorType = type;
	}
	
	/**
	 * Gets the message for this error
	 * @return the message for this error
	 */
	public String getErrorMsg(){
		return error.getMessage();
	}
	
	/**
	 * Gets the exception that was thrown to create this error
	 * @return the original error
	 */
	public Exception getError(){
		return error;
	}

	/**
	 * Gets the specific error type of this error
	 * @return the specific error type
	 */
	public DeaconErrorType getErrorType(){
		return errorType;
	}
	
}
