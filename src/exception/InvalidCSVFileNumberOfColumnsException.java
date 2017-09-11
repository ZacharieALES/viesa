//Copyright (C) 2012 Zacharie ALES and Rick MORITZ
//
//This file is part of Viesa.
//
//Viesa is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Viesa is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with Viesa.  If not, see <http://www.gnu.org/licenses/>.

package exception;

/**
 * Exception called when the number of columns in one line of the csv file is not the same that the one of the first line of the file
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class InvalidCSVFileNumberOfColumnsException extends AbstractException{

	private String fileName;
	
	private int currentLineNumber;
	private int currentLineColumnNumber;
	
	private int firstLineColumnNumber;
	
	public InvalidCSVFileNumberOfColumnsException(String fileName, int currentLineNumber, int currentLineColumnNumber, int firstLineColumnNumber){

		this.fileName = fileName;
		this.currentLineColumnNumber = currentLineColumnNumber;
		this.currentLineNumber = currentLineNumber;
		this.firstLineColumnNumber = firstLineColumnNumber;
	}

	@Override
	public String defaultMessage() {
		return "Unable to add file \"" + fileName + "\". Its first line contains " + firstLineColumnNumber + " columns however, its line number " + currentLineNumber + " contains " + currentLineColumnNumber + " columns";
	}
	
}
