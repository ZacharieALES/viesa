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
 * Expression called when the number of columns in the input file is to low compared to the one used in the column format
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class InvalidNumberOfColumnsInInputFilesException extends AbstractException {
	
	private String fileName;
	private int requiredNumberOfColumns;
	private int currentNumberOfColumns;
	
	public InvalidNumberOfColumnsInInputFilesException(String fileName, int requiredNumberOfColumns, int currentNumberOfColumns){
		super();

		this.fileName = fileName;
		this.requiredNumberOfColumns = requiredNumberOfColumns;
		this.currentNumberOfColumns = currentNumberOfColumns;
		
		System.err.println(defaultMessage());
	}

	@Override
	public String defaultMessage() {
		return "Cannot add the file \"" + fileName + "\" it only contains " + currentNumberOfColumns + " columns and the previously specified format requires " + requiredNumberOfColumns + " columns. ";
	}

}
