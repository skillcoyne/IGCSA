select cgi.CaseNo, cgi.Tissue, cgi.Clones, cgi.KaryShort, ka.Abnormality
from mitelman.cytogeninv cgi
inner join mitelman.karyabnorm ka on ka.CaseNo = cgi.CaseNo
