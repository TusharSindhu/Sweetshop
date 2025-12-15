export default function SweetCard({ sweet, onPurchase }) {
  // We'll use a placeholder image since we aren't handling file uploads yet
  const imageUrl = "https://placehold.co/400x300/orange/white?text=" + sweet.name.replace(" ", "+");

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow duration-300 border border-brand-100">
      <img src={imageUrl} alt={sweet.name} className="w-full h-48 object-cover" />
      
      <div className="p-4">
        <div className="flex justify-between items-start">
          <div>
            <h3 className="text-xl font-bold text-brand-900">{sweet.name}</h3>
            <span className="inline-block bg-brand-100 text-brand-800 text-xs px-2 py-1 rounded-full mt-1">
              {sweet.category}
            </span>
          </div>
          <span className="text-lg font-bold text-brand-600">₹{sweet.price}</span>
        </div>

        <div className="mt-4 flex justify-between items-center">
          <span className={`text-sm ${sweet.quantity > 0 ? 'text-green-600' : 'text-red-600 font-bold'}`}>
            Stock: {sweet.quantity}
          </span>
          
          <button 
            onClick={() => onPurchase(sweet.id)}
            disabled={sweet.quantity === 0}
            className={`px-4 py-2 rounded-md font-medium text-white transition-colors
              ${sweet.quantity > 0 
                ? 'bg-brand-500 hover:bg-brand-600' 
                : 'bg-gray-400 cursor-not-allowed'}`}
          >
            {sweet.quantity > 0 ? 'Purchase' : 'Sold Out'}
          </button>
        </div>
      </div>
    </div>
  );
}