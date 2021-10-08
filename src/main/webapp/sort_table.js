function compareValues(a, b) {
  // return -1/0/1 based on what you "know" a and b
  // are here. Numbers, text, some custom case-insensitive
  // and natural number ordering, etc. That's up to you.
  // A typical "do whatever JS would do" is:
  return (a<b) ? -1 : (a>b) ? 1 : 0;
}

function compareValuesNumAsc(a, b) {
	 var _a=Number(a)
	 var _b = Number(b)
	  return (_a<_b) ? 1 : (_a>_b) ? -1 : 0;
}

function compareValuesNumDesc(a, b) {
	 var _a=Number(a)
	 var _b = Number(b)
	  return (_a<_b) ? -1 : (_a>_b) ? 1 : 0;
}

// by default: ascending
function sortTable(colnum, descending='false') {
  // get all the rows in this table:
  let rows = Array.from(table.querySelectorAll(`tr`));

  // but ignore the heading row:
  rows = rows.slice(1);

  // set up the queryselector for getting the indicated
  // column from a row, so we can compare using its value:
  let qs = `td:nth-child(${colnum})`;

  // and then just... sort the rows:
  rows.sort( (r1,r2) => {
    // get each row's relevant column
    let t1 = r1.querySelector(qs);
    let t2 = r2.querySelector(qs);

	//return isNaN(t1.textContent) ? compareValues(t1.textContent,t2.textContent) : compareValuesNumAsc(t1.textContent,t2.textContent);

    if(descending=='false'){
    	// and then effect sorting by comparing their content:
    	return isNaN(t1.textContent) ? compareValues(t1.textContent,t2.textContent) : compareValuesNumDesc(t1.textContent,t2.textContent);
    }else{
    	return isNaN(t1.textContent) ? compareValues(t1.textContent,t2.textContent) : compareValuesNumAsc(t1.textContent,t2.textContent);
    }
  });

  // and then the magic part that makes the sorting appear on-page:
  rows.forEach(row => table.appendChild(row));
}

//table.querySelectorAll(`th`).forEach((th, position) => {
//	  th.addEventListener(`click`, evt => sortTable(position));
//});