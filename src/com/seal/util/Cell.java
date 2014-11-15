 
package com.seal.util;

public class Cell {
  
  public double value = 0;
  
  public Cell() {}
  
  public Cell(double value) {
    this.value = value;
  }
  
  public String toString() {
    return Double.toString(value);
  }
}
