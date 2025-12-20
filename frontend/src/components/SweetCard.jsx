export default function SweetCard({ sweet, onPurchase, onEdit, onRestock, onDelete, isAdmin }) {
  // Placeholder image based on name
  const imageUrl = "https://placehold.co/400x300/orange/white?text=" + sweet.name.replace(" ", "+");

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow duration-300 border border-brand-100 flex flex-col h-full">
      <img src={imageUrl} alt={sweet.name} className="w-full h-48 object-cover" />
      
      <div className="p-4 flex-1 flex flex-col">
        <div className="flex justify-between items-start mb-2">
          <div>
            <h3 className="text-xl font-bold text-brand-900">{sweet.name}</h3>
            <span className="inline-block bg-brand-100 text-brand-800 text-xs px-2 py-1 rounded-full mt-1">
              {sweet.category}
            </span>
          </div>
          <span className="text-lg font-bold text-brand-600">₹{sweet.price}</span>
        </div>

        <div className="mt-auto">
          <div className="flex justify-between items-center mb-3">
            <span className={`text-sm ${sweet.quantity > 0 ? 'text-green-600' : 'text-red-600 font-bold'}`}>
              Stock: {sweet.quantity}
            </span>
            
            <button 
              onClick={() => onPurchase(sweet.id)}
              disabled={sweet.quantity === 0}
              className={`px-3 py-1 rounded-md text-sm font-medium text-white transition-colors
                ${sweet.quantity > 0 
                  ? 'bg-brand-500 hover:bg-brand-600' 
                  : 'bg-gray-400 cursor-not-allowed'}`}
            >
              {sweet.quantity > 0 ? 'Buy' : 'Sold Out'}
            </button>
          </div>

          {/* ADMIN CONTROLS */}
          {isAdmin && (
            <div className="grid grid-cols-3 gap-2 border-t pt-3 mt-2">
              <button onClick={() => onRestock(sweet.id)} className="bg-blue-100 text-blue-700 text-xs py-1 rounded hover:bg-blue-200 font-bold">
                + Stock
              </button>
              <button onClick={() => onEdit(sweet)} className="bg-yellow-100 text-yellow-700 text-xs py-1 rounded hover:bg-yellow-200 font-bold">
                Edit
              </button>
              <button onClick={() => onDelete(sweet.id)} className="bg-red-100 text-red-700 text-xs py-1 rounded hover:bg-red-200 font-bold">
                Delete
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}